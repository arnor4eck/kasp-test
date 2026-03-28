package com.arnor4eck.index;

import jdk.jfr.Description;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

public class DefaultIndexTest {

    private final Index index = new Index();

    @TempDir
    private Path tempDir;

    // Index file
    @Test
    public void testShouldSaveOnlyOnce() throws IOException {
        Path file1 = tempDir.resolve("file1.txt");
        Files.writeString(file1, "test");

        boolean firstSave = index.addFile(file1.toAbsolutePath().toString());
        boolean secondSave = index.addFile(file1.toAbsolutePath().toString());

        Assertions.assertTrue(firstSave);
        Assertions.assertFalse(secondSave);
        Assertions.assertEquals(1, index.getIndexedFiles().size());
    }

    @Test
    @Description("При попытке сохранить несуществующий файл выбрасывается IndexException")
    public void testSaveFileShouldThrowExceptionFileDoesntExist() {
        Path file1 = tempDir.resolve("file1.txt");
        String path = file1.toAbsolutePath().toString();

        Throwable ex = Assertions.assertThrows(IndexException.class, () -> index.addFile(path));

        Assertions.assertTrue(ex.getMessage().startsWith("File does not exist"));
    }

    @Test
    @Description("При попытке передать null выдается предупреждение")
    public void testSaveFileNullShouldThrowException() {
        Throwable ex = Assertions.assertThrows(IndexException.class, () -> index.addFile(null));

        Assertions.assertEquals("Path could`t be null", ex.getMessage());
    }

    @Test
    @Description("При попытке индексирования неподдерживаемого файла выдается предупреждение")
    public void testSaveUnsupportedFileNullShouldThrowException() throws IOException {
        Path filePdf = tempDir.resolve("file1.pdf");
        Files.writeString(filePdf, "unsupported");

        Throwable ex = Assertions.assertThrows(IndexException.class, () -> index.addFile(filePdf.toAbsolutePath().toString()));

        Assertions.assertTrue(ex.getMessage().startsWith("Unsupported file format"));
    }

    // Indexing Dir
    @Test
    public void testShouldSaveDir() throws IOException {
        Path file1 = tempDir.resolve("file1.txt");
        Path file2 = tempDir.resolve("file2.txt");
        Path file3 = tempDir.resolve("file3.pdf");
        Files.writeString(file1, "file1");
        Files.writeString(file2, "file2");
        Files.writeString(file3, "file3 incorrect suffix");

        index.addDir(tempDir.toAbsolutePath().toString(), false);

        Assertions.assertEquals(2, index.getIndexedFiles().size());
    }

    @Test
    @Description("При попытке индексирования НЕ-директории выдается предупреждение")
    public void testSaveNotDirShouldThrowException() {
        Path notADir = tempDir.resolve("file1.txt");

        Throwable ex = Assertions.assertThrows(IndexException.class,
                () -> index.addDir(notADir.toAbsolutePath().toString(), false));

        Assertions.assertTrue(ex.getMessage().startsWith("Not a directory"));
    }

    @Test
    @Description("При попытке индексирования несуществующей директории выдается предупреждение")
    public void testSaveNotExistedDirShouldThrowException() {
        Path notADir = tempDir.resolve("notExists");

        Throwable ex = Assertions.assertThrows(IndexException.class,
                () -> index.addDir(notADir.toAbsolutePath().toString(), false));

        Assertions.assertTrue(ex.getMessage().startsWith("Not a directory"));
    }

    @Test
    @Description("При попытке передачи null выдается предупреждение")
    public void testSaveNullShouldThrowException() {
        Throwable ex = Assertions.assertThrows(IndexException.class,
                () -> index.addDir(null, false));

        Assertions.assertEquals("Path could`t be null", ex.getMessage());
    }


    // Remove file
    @Test
    public void testShouldRemoveFile() throws IOException {
        Path file1 = tempDir.resolve("file1.txt");
        String path = file1.toAbsolutePath().toString();
        Files.writeString(file1, "file1");

        index.addFile(path);
        Assertions.assertEquals(1, index.getIndexedFiles().size());

        Assertions.assertTrue(index.removeFile(path));
        Assertions.assertEquals(0, index.getIndexedFiles().size());
    }

    @Test
    public void testRemoveUnindexedFileShouldReturnFalse() throws IOException {
        Path file1 = tempDir.resolve("file1.txt");
        Path file2 = tempDir.resolve("file2.txt");

        Files.writeString(file1, "file1");
        Files.writeString(file2, "file2");

        String path = file1.toAbsolutePath().toString();

        index.addFile(path);
        Assertions.assertEquals(1, index.getIndexedFiles().size());

        Assertions.assertFalse(index.removeFile(file2.toAbsolutePath().toString()));
        Assertions.assertEquals(1, index.getIndexedFiles().size());
    }

    @Test
    @Description("При попытке передачи null выдается предупреждение")
    public void testRemoveNullShouldThrowException() {
        Throwable ex = Assertions.assertThrows(IndexException.class,
                () -> index.removeFile(null));

        Assertions.assertEquals("Path could`t be null", ex.getMessage());
    }

    // Search token
    @Test
    public void testShouldSearchInfo() throws IOException {
        Path file1 = tempDir.resolve("file1.txt");
        String path = file1.toAbsolutePath().toString();

        String token = "file";

        Files.writeString(file1, token);

        index.addFile(path);
        Assertions.assertEquals(1, index.getIndexedFiles().size());

        Set<String> files = index.search(token);
        Assertions.assertFalse(files.isEmpty());
        Assertions.assertEquals(1, files.size());
        Assertions.assertTrue(files.contains(path));
    }

    @Test
    public void testShouldNotFind() throws IOException {
        Path file1 = tempDir.resolve("file1.txt");
        String path = file1.toAbsolutePath().toString();

        String token = "file";
        String notToken = token + System.currentTimeMillis();

        Files.writeString(file1, token);

        index.addFile(path);
        Assertions.assertEquals(1, index.getIndexedFiles().size());

        Set<String> files = index.search(notToken);
        Assertions.assertTrue(files.isEmpty());
    }

    @Test
    @Description("При попытке передачи null выдается предупреждение")
    public void testSearchNullShouldThrowException() {
        Throwable ex = Assertions.assertThrows(IndexException.class,
                () -> index.search(null));

        Assertions.assertEquals(ex.getMessage(), "Token could`t be null");
    }
}
