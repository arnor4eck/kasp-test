package com.arnor4eck;

import com.arnor4eck.index.Index;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

public class Main {
    private static final String PROMPT = "Введите команду: ";

    public static void main(String[] args) throws UnsupportedEncodingException {

        System.setOut(new java.io.PrintStream(System.out, true, "UTF-8"));

        Index index;
        try {
            index = new Index();
        } catch (IOException e) {
            System.err.printf("Ошибка инициализации индекса: %s", e.getMessage());
            return;
        }

        System.out.println("=== Индексация текстовых файлов ===");
        printHelp();
        System.out.println();

        try (Scanner in = new Scanner(System.in)) {
            String line;
            while (true) {
                System.out.print(PROMPT);
                line = in.nextLine();
                if (line == null)
                    break;


                line = line.trim();
                if (line.isEmpty())
                    continue;

                String[] parts = line.split(" ", 2);
                String command = parts[0].toLowerCase();
                String arg = parts.length > 1 ? parts[1] : null;

                try {
                    switch (command) {
                        case "add" -> handleAdd(index, arg);
                        case "adddir" -> handleAddDir(index, arg, false);
                        case "adddirr" -> handleAddDir(index, arg, true);
                        case "search" -> handleSearch(index, arg);
                        case "list" -> handleList(index);
                        case "remove" -> handleRemove(index, arg);
                        case "help" -> printHelp();
                        case "exit", "q" -> {
                            System.out.println("Выход");
                            return;
                        }
                        default -> System.out.println("Неизвестная команда: '%s' Введите 'help' для справки.");
                    }
                } catch (Exception e) {
                    System.out.printf("Ошибка: %s%n", e.getMessage());
                }
            }
        }
    }

    private static void handleAdd(Index index, String path) {
        if (path == null || path.isBlank()) {
            System.out.println("Использование: add <путь_к_файлу>");
            return;
        }

        if (index.addFile(path.trim()))
            System.out.printf("Файл добавлен в индекс: %s%n", path.trim());
        else
            System.out.printf("Файл уже в индексе: %s%n", path.trim());
    }

    private static void handleAddDir(Index index, String path, boolean recursive) {
        if (path == null || path.isBlank()) {
            System.out.println("Использование: adddir <путь_к_каталогу>");
            return;
        }

        try {
            index.addDir(path.trim(), recursive);
            System.out.printf("Директория добавлена в индекс: %s %s%n", path.trim(),
                (recursive ? " (рекурсивно)" : ""));
        } catch (IOException e) {
            System.out.printf("Ошибка: %s%n", e.getMessage());
        }
    }

    private static void handleSearch(Index index, String word) {
        if (word == null || word.isBlank()) {
            System.out.println("Использование: search <слово>");
            return;
        }

        Set<String> res = index.search(word.trim());

        if (res.isEmpty())
            System.out.println("Ничего не найдено");
        else {
            System.out.printf("Найдено файлов: %d%n", res.size());
            for (String f : res)
                System.out.printf("\t%s%n", f);
        }
    }

    private static void handleList(Index index) {
        List<String> files = index.getIndexedFiles();

        if (files.isEmpty())
            System.out.println("Пустой индес");
        else {
            System.out.printf("Файлов в индексе: %d%n", files.size());
            for (String f : files)
                System.out.printf("\t%s%n", f);
        }
    }

    private static void handleRemove(Index index, String path) {
        if (path == null || path.isBlank()) {
            System.out.println("Использование: remove <путь_к_файлу>");
            return;
        }

        if (index.removeFile(path.trim()))
            System.out.printf("Файл удалён из индекса: %s%n", path.trim());
        else
            System.out.printf("Файл не найден в индексе: %s%n", path.trim());
    }

    private static void printHelp() {
        System.out.println("Команды:");
        System.out.println("\tadd <path>\t- добавить файл в индекс;");
        System.out.println("\tadddir <path>\t- добавить каталог в индекс;");
        System.out.println("\tadddirr <path>\t- добавить каталог рекурсивно;");
        System.out.println("\tsearch <word>\t- поиск файлов по слову;");
        System.out.println("\tlist\t- показать все индексированные файлы;");
        System.out.println("\tremove <path>\t- удалить файл из индекса;");
        System.out.println("\thelp\t- показать эту справку;");
        System.out.println("\texit / q\t- выход");
    }
}