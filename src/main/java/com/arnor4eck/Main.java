package com.arnor4eck;

import com.arnor4eck.index.Index;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

public class Main {
    private static final String PROMPT = "Enter command: ";

    public static void main(String[] args) throws UnsupportedEncodingException {

        System.setOut(new java.io.PrintStream(System.out, true, "UTF-8"));
        System.setErr(new java.io.PrintStream(System.err, true, "UTF-8"));

        Index index;
        try {
            index = new Index();
        } catch (IOException e) {
            System.err.printf("Index initialization error: %s", e.getMessage());
            return;
        }

        System.out.println("\t\tText File Indexing");
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
                            System.out.println("Exiting");
                            return;
                        }
                        default -> System.out.println("Unknown command: '%s'. Type 'help' for instructions.");
                    }
                } catch (Exception e) {
                    System.out.printf("Error: %s%n", e.getMessage());
                }
            }
        }
    }

    private static void handleAdd(Index index, String path) {
        if (path == null || path.isBlank()) {
            System.out.println("Usage: add <file_path>");
            return;
        }

        if (index.addFile(path.trim()))
            System.out.printf("File added to index: %s%n", path.trim());
        else
            System.out.printf("File already in index: %s%n", path.trim());
    }

    private static void handleAddDir(Index index, String path, boolean recursive) {
        if (path == null || path.isBlank()) {
            System.out.println("Usage: adddir <directory_path>");
            return;
        }

        try {
            index.addDir(path.trim(), recursive);
            System.out.printf("Directory added to index: %s %s%n", path.trim(),
                (recursive ? " (recursively)" : ""));
        } catch (IOException e) {
            System.out.printf("Error: %s%n", e.getMessage());
        }
    }

    private static void handleSearch(Index index, String word) {
        if (word == null || word.isBlank()) {
            System.out.println("Usage: search <word>");
            return;
        }

        Set<String> res = index.search(word.trim());

        if (res.isEmpty())
            System.out.println("Nothing found");
        else {
            System.out.printf("Files found: %d%n", res.size());
            for (String f : res)
                System.out.printf("\t%s%n", f);
        }
    }

    private static void handleList(Index index) {
        List<String> files = index.getIndexedFiles();

        if (files.isEmpty())
            System.out.println("Index is empty");
        else {
            System.out.printf("Files in index: %d%n", files.size());
            for (String f : files)
                System.out.printf("\t%s%n", f);
        }
    }

    private static void handleRemove(Index index, String path) {
        if (path == null || path.isBlank()) {
            System.out.println("Usage: remove <file_path>");
            return;
        }

        if (index.removeFile(path.trim()))
            System.out.printf("File removed from index: %s%n", path.trim());
        else
            System.out.printf("File not found in index: %s%n", path.trim());
    }

    private static void printHelp() {
        System.out.println("Commands:");
        System.out.println("\tadd <path>\t- add a file to the index;");
        System.out.println("\tadddir <path>\t- add a directory to the index;");
        System.out.println("\tadddirr <path>\t- add a directory recursively;");
        System.out.println("\tsearch <word>\t- search files by word;");
        System.out.println("\tlist\t- show all indexed files;");
        System.out.println("\tremove <path>\t- remove a file from the index;");
        System.out.println("\thelp\t- show this help;");
        System.out.println("\texit / q\t- exit");
    }
}