package com.arnor4eck.index;

import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

public final class Index {
    private final FileReader reader;
    private final Tokenizer tokenizer;
    private final Normalizer normalizer;
    private final Searcher searcher;

    private final Map<String, Set<SearchResult>> results;
    private final Set<SearchResult> indexedFiles;
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    public Index(FileReader reader, Tokenizer tokenizer,
                 Normalizer normalizer, Searcher searcher) {
        this.reader = reader;
        this.tokenizer = tokenizer;
        this.normalizer = normalizer;
        this.searcher = searcher;

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
}
