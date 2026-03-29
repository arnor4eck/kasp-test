package com.arnor4eck.index;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

/** Сервис мониторинга директорий и индексации файлов в реальном времени
 *
 * @see IndexHandler
 */
public class DirScanner {
    private final ExecutorService dirScannerExecutor;
    private final ExecutorService indexingExecutor;
    private final WatchService watcher;
    private final BlockingQueue<Event> watchQueue;
    private final AtomicBoolean run = new AtomicBoolean(false);
    private final IndexHandler handler;
    private final List<String> suffixes;

    private final long QUEUE_TIMEOUT = 500;

    /** Конструктор
     * @param handler обработчик событий индексации (not null)
     * @param suffixes список поддерживаемых расширений файлов
     * @throws IOException если не удалось создать WatchService
     * @throws NullPointerException если handler или suffixes равны null
     */
    public DirScanner(IndexHandler handler, List<String> suffixes) throws IOException {
        this.watcher = FileSystems.getDefault().newWatchService();
        this.dirScannerExecutor = Executors.newVirtualThreadPerTaskExecutor();
        this.watchQueue = new LinkedBlockingQueue<>();
        this.indexingExecutor = Executors.newVirtualThreadPerTaskExecutor();
        this.handler = Objects.requireNonNull(handler);
        this.suffixes = Objects.requireNonNull(suffixes);
    }

    /** Типы событий для внутренней передачи между ExecutorService`s
     * */
    private enum EventType {
        INIT, OVERFLOW, ADDED, REMOVED, UPDATED;

        /** Преобразует событие WatchService в тип EventType
         * @param kind тип события от WatchService
         * @return EventType - соответствующий переданному типу
         */
        public static DirScanner.EventType fromWatchEvents(WatchEvent.Kind<?> kind) {
            if (kind == StandardWatchEventKinds.ENTRY_CREATE) { return EventType.ADDED; }
            else if (kind == StandardWatchEventKinds.ENTRY_DELETE) { return EventType.REMOVED; }
            else if (kind == StandardWatchEventKinds.ENTRY_MODIFY) { return EventType.UPDATED; }
            else if (kind == StandardWatchEventKinds.OVERFLOW) { return EventType.OVERFLOW; }

            return EventType.INIT;
        }
    }

    /** Внутренняя запись события
     * @param path путь к файлу или директории
     * @param type тип события
     */
    private record Event(Path path, EventType type) {}

    /** Добавляет директорию в наблюдение и ставит в очередь в индекс
     * @param dir путь к директории для наблюдения
     * @param recursive если true, директория сканируется рекурсивно со всеми поддиректориями
     * @throws IOException если директория не существует или произошла ошибка при регистрации
     * @throws IndexException если директория не существует или не является директорией
     */
    public void addDirToWatcher(String dir, boolean recursive) throws IOException {
        Path path = Paths.get(dir);

        if(!Files.exists(path) || !Files.isDirectory(path))
            throw new IndexException(String.format("Directory does`t exists: %s",
                                    path.toAbsolutePath()));

        watchQueue.add(new Event(path, EventType.INIT));
        this.addDir(path);

        if(recursive){
            try(Stream<Path> dirs = Files.walk(path, FileVisitOption.FOLLOW_LINKS)
                        .skip(1)
                        .filter(Files::isDirectory)) {
                for (Path subDir : dirs.toList())
                    this.addDir(subDir);
            }catch (IOException e){
                throw new IndexException(e.getMessage());
            }
        }

    }

    /** Регистрирует директорию в WatchService и добавляет её файлы в индекс
     */
    private void addDir(Path dir) throws IOException {
        dir.register(watcher, // регистрация мониторинга
            StandardWatchEventKinds.ENTRY_CREATE,
            StandardWatchEventKinds.ENTRY_DELETE,
            StandardWatchEventKinds.ENTRY_MODIFY);

        try(Stream<Path> files = Files.list(dir)
                .filter(Files::isRegularFile)
                .filter(this::isSupportedFile)){
            files.forEach(path -> { watchQueue.add(new Event(path, EventType.INIT)); });
        }
    }

    /** Запускает поток наблюдения за событиями в файловой системе
     */
    private void startWatching(){
        dirScannerExecutor.submit(() -> {
            while(run.get()){
                try {
                    WatchKey key = watcher.poll(QUEUE_TIMEOUT, TimeUnit.MILLISECONDS);
                    if (key == null) continue;

                    Path dirPath = (Path) key.watchable();

                    for (WatchEvent<?> event : key.pollEvents()) {
                        WatchEvent.Kind<?> kind = event.kind();

                        @SuppressWarnings("unchecked") // приведение корректно, поскольку всегда возвращает <Path>
                        Path relativePath = ((WatchEvent<Path>) event).context();

                        Path fullPath = dirPath.resolve(relativePath);
                        watchQueue.add(new Event(fullPath, EventType.fromWatchEvents(kind)));
                    }

                    key.reset();
                }catch (InterruptedException e){
                    Thread.currentThread().interrupt();
                }
            }
        });
    }

    /** Запускает поток индексации событий в файловой системе
     */
    private void startIndexing(){
        indexingExecutor.submit(() -> {
            while(run.get()){
                try{
                    Event event = watchQueue.poll(QUEUE_TIMEOUT, TimeUnit.MILLISECONDS);

                    if(event == null) continue;
                    Path path = event.path;

                    switch (event.type){
                        case INIT:
                        case ADDED:
                            if(Files.isDirectory(path))
                                handler.onDirCreate(path);
                            else if(Files.isRegularFile(path))
                                handler.onFileCreate(path);
                            break;
                        case REMOVED:
                            handler.onRemove(path);
                            break;
                        case UPDATED:
                            if(Files.isRegularFile(path))
                                handler.onFileUpdate(path);
                            break;
                        case OVERFLOW:
                            handler.onOverflow(path);
                            break;
                    }

                } catch (InterruptedException e){
                    Thread.currentThread().interrupt();
                }
            }
        });
    }

    /** Проверяет разрешение файла
     * @param file Проверяемый файл
     * @return boolean - Поддерживается ли данный файл
     * */
    private boolean isSupportedFile(Path file){
        return suffixes
                .stream()
                .anyMatch(suffix -> file.getFileName().toString().endsWith(suffix));
    }

    /** Запускает сервис мониторинга и обработки событий
     */
    public void start(){
        run.set(true);
        this.startWatching();
        this.startIndexing();
    }

    /** Останавливает сервис и освобождает ресурсы
     * @throws IndexException если не удалось закрыть WatchService
     */
    public void stop(){
        run.set(false);
        dirScannerExecutor.shutdown();
        indexingExecutor.shutdown();

        try {
            if (!dirScannerExecutor.awaitTermination(1, TimeUnit.SECONDS)) {
                dirScannerExecutor.shutdownNow();
            }
            if (!indexingExecutor.awaitTermination(1, TimeUnit.SECONDS)) {
                indexingExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            dirScannerExecutor.shutdownNow();
            indexingExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
        try {
            watcher.close();
        } catch (IOException e) {
            throw new IndexException("Failed to close WatchService", e);
        }
    }
}
