package com.arnor4eck.index;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/** Интерфейс для чтения файлов
 * */
public interface FileReader {
    /** Читает содержимое файла
     * @param file Файл, подлежащий чтению
     * @return String - содержимое файла
     * */
    String readFile(Path file) throws IOException;

    /** Возвращает поддерживыемые разрешения файлов
     * @return List<String> - разрешения файлов
     * */
    List<String> getPossibleSuffixes();
}
