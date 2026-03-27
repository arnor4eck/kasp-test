package com.arnor4eck.index;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

final class TxtFileReader implements FileReader {

    private final List<String> suffixes = Collections.singletonList("txt");

    @Override
    public String readFile(Path file) throws IOException {
        return new String(Files.readAllBytes(file));
    }

    @Override
    public List<String> getPossibleSuffixes() {
        return suffixes;
    }
}
