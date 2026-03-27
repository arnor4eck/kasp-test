package com.arnor4eck.index;

/** Интерфейс для нормализации токена (слова)
 * */
public interface Normalizer {
    /** Нормализация токена
     * @param token Слово, которое необходимо нормализовать
     * @return String - нормальная форма
     * */
    String normalize(String token) throws IllegalArgumentException;
}
