package com.arnor4eck.index;

import java.util.Map;
import java.util.Set;

/** Интерфейс логики поиска
 * */
public interface Searcher {
    /** Поиск файлов по запросу
     * @param query поисковый запрос
     * @return Set<SearchResult> - множество результатов поиска
     */
    Set<SearchResult> search(Map<String, Set<SearchResult>> results, String query);
}
