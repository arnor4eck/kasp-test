package com.arnor4eck.index;

import java.util.Set;

/** Интерфейс, разбивающий текст на токены (слова)
 * */
public interface Tokenizer {
    /** Разбивка текста на токены
     * @param text Исходный текст
     * @return Set<String> - множество полученных токенов
     * */
    Set<String> tokenize(String text);
}
