package com.arnor4eck.index;

import java.nio.file.Path;

/** Интерфейс обработчика событий индексации
 * @see DirScanner
 * @see Index
 * @see DefaultIndexHandler
 */
public interface IndexHandler {
    /** Вызывается при создании новой директории
     * @param path путь к созданной директории
     */
    void onDirCreate(Path path);

    /** Вызывается при создании нового файла
     * @param path путь к созданному файлу
     */
    void onFileCreate(Path path);

    /** Вызывается при изменении файла (модификация содержимого)
     * @param path путь к изменённому файлу
     */
    void onFileUpdate(Path path);

    /** Вызывается при удалении файла
     * @param path путь к удалённому файлу
     */
    void onRemove(Path path);

    /** Вызывается при потере события
     * @param path путь к директории, где произошла потеря
     */
    void onOverflow(Path path);
}
