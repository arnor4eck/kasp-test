package com.arnor4eck.index;

import java.nio.file.Path;

record SearchResult(String filePath) {
    @Override
    public String toString() {
        return filePath;
    }

    public SearchResult(Path file) {
        this(file.toAbsolutePath().toString());
    }

    @Override
    public boolean equals(final Object other) {
        if(!(other instanceof SearchResult))
            return false;
        return this.filePath.equals(((SearchResult) other).filePath);
    }

    @Override
    public int hashCode() {
        return filePath.hashCode();
    }
}
