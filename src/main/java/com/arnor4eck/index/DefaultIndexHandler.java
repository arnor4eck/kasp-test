package com.arnor4eck.index;

import java.nio.file.Files;
import java.nio.file.Path;

class DefaultIndexHandler implements IndexHandler {

    private final Index index;

    public DefaultIndexHandler(Index index) {
        this.index = index;
    }

    @Override
    public void onDirCreate(Path path) {}

    @Override
    public void onFileCreate(Path path) {
        if (Files.isRegularFile(path))
            index.addFile(path.toAbsolutePath().toString());
    }

    @Override
    public void onFileUpdate(Path path) {
        index.removeFile(path.toAbsolutePath().toString());
        index.addFile(path.toAbsolutePath().toString());
    }

    @Override
    public void onRemove(Path path) {
        index.removeFile(path.toAbsolutePath().toString());
    }

    @Override
    public void onOverflow(Path path) {
        throw new IndexException(String.format("Index overflow: %s", path.toAbsolutePath()));
    }
}
