package com.arnor4eck.index;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class Index {
    private final FileReader reader;
    private final Tokenizer tokenizer;
    private final Normalizer normalizer;
    private final Map<String, Set<SearchResult>> results;

    public Index(FileReader reader,
                 Tokenizer tokenizer,
                 Normalizer normalizer) {
        this.reader = reader;
        this.tokenizer = tokenizer;
        this.normalizer = normalizer;
        this.results = new ConcurrentHashMap<>();
    }
}
