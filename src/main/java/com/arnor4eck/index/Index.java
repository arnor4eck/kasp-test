package com.arnor4eck.index;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class Index {
    private final FileReader reader;
    private final Tokenizer tokenizer;
    private final Normalizer normalizer;

    private final Map<String, Set<SearchResult>> results;
    private final Set<SearchResult> indexedFiles;
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    public Index(){
        this(new TxtFileReader(),
            new FullWordsTokenizer(),
            new ToLowerCaseNormalizer());
    }

    public Index(FileReader reader,
                 Tokenizer tokenizer,
                 Normalizer normalizer) {
        this.reader = reader;
        this.tokenizer = tokenizer;
        this.normalizer = normalizer;

        this.results = new HashMap<>();
        this.indexedFiles = new HashSet<>();
    }

    /** Добавление файла в индекс
     * @param path Путь до файла
     * @return boolean - Результат выполнения
     * @throws IndexException
     * */
    public boolean addFile(String path) {
        lock.writeLock().lock();

        try {
            Path newFile = Path.of(path);

            if(!this.isSupportedFile(newFile))
                throw new IllegalArgumentException(String.format("Неподдерживаемый формат файла: %s",
                        newFile.toAbsolutePath()));

            SearchResult newIndexedFile = new SearchResult(newFile);
            if (indexedFiles.contains(newIndexedFile))
                return false;

            if (!Files.exists(newFile))
                throw new NoSuchFileException(String.format("Файла не существует: %s",
                        newFile.toAbsolutePath()));

            String content = reader.readFile(newFile);
            Set<String> tokens = tokenizer.tokenize(content);

            Set<String> normalizedTokens = tokens.stream()
                    .map(normalizer::normalize)
                    .collect(Collectors.toSet());

            this.addToIndex(newIndexedFile, normalizedTokens);

            return true;
        }catch (Exception e) {
            throw new IndexException(e.getMessage(), e);
        } finally {
            lock.writeLock().unlock();
        }
    }

    /** Добавление каталога в индекс
     * @param path Путь к кталогу
     * @param recursive Необходимо ли рекурсивно проходить в поддиректории
     * */
    public void addDir(String path,
                          boolean recursive) throws IOException {
        Path dir = Path.of(path);
        if (!Files.exists(dir) || !Files.isDirectory(dir))
            throw new IndexException(String.format("Не является директорией: %s",
                    dir.toAbsolutePath()));

        List<Path> indexedFiles;
        try(Stream<Path> files = recursive ? Files.walk(dir) : Files.list(dir)){
            indexedFiles = files
                    .filter(Files::isRegularFile)
                    .filter(this::isSupportedFile)
                    .toList();
        }

        lock.writeLock().lock();
        try {
            for(Path file : indexedFiles)
                this.addFile(file.toAbsolutePath().toString());
        }finally {
            lock.writeLock().unlock();
        }
    }

    /** @see Index#addFile(String)  Вспомогательная функция для добавления файла в индекс
     * @param searchResult Добавляемый файл
     * @param tokens Добавляемые токены
     * */
    private void addToIndex(SearchResult searchResult,
                            Set<String> tokens){
        for(String token : tokens){
            results.compute(token, (tok, filesSet) -> {

                if(filesSet == null)
                    filesSet = new HashSet<>();
                filesSet.add(searchResult);

                return filesSet;
            });
        }

        indexedFiles.add(searchResult);
    }

    /** Проверяет разрешение файла
     * @param file Проверяемый файл
     * @return boolean - Поддерживается ли данный файл
     * */
    private boolean isSupportedFile(Path file){
        return reader.getPossibleSuffixes()
                .stream()
                .anyMatch(suffix -> file.getFileName().toString().endsWith(suffix));
    }

    /** Удаление файла из индекса
     * @param path Путь к удаляемому файлу
     * @return boolean - Результат выполнения
     * */
    public boolean removeFile(String path){
        lock.writeLock().lock();
        try{
            SearchResult removedFile = new SearchResult(Path.of(path.trim()).toAbsolutePath());
            if(!indexedFiles.contains(removedFile))
                return false;

            Iterator<Set<SearchResult>> it = results.values().iterator();

            while(it.hasNext()){
                Set<SearchResult> set = it.next();
                set.remove(removedFile); // удаляем файл от токена

                if(set.isEmpty()) // если этого токена больше нет в других файлах - удаляем и сам токен
                    it.remove();
            }

            indexedFiles.remove(removedFile);
            return true;
        }
        finally {
            lock.writeLock().unlock();
        }
    }

    /** Поиск файлов по слову
     * @param token Токен, по которому ведётся поиск
     * @return Set<String> - Множество файлов, в которых присутствует токен
     * */
    public Set<String> search(String token){
        lock.readLock().lock();
        try{
            return results.get(normalizer.normalize(token)).stream()
                    .map(SearchResult::filePath).collect(Collectors.toSet());
        }finally {
            lock.readLock().unlock();
        }
    }

    public List<String> getIndexedFiles(){
        lock.readLock().lock();
        try{
            return indexedFiles.stream()
                    .map(SearchResult::filePath)
                    .collect(Collectors.toList());
        }finally {
            lock.readLock().unlock();
        }
    }
}
