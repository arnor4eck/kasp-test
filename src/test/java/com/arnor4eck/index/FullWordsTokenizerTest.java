package com.arnor4eck.index;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Set;

class FullWordsTokenizerTest {

    private final Tokenizer tokenizer = new FullWordsTokenizer();

    @Test
    public void testEmptyText() {
        Set<String> tokens = tokenizer.tokenize("");

        Assertions.assertTrue(tokens.isEmpty());
    }

    @Test
    public void testNull(){
        Set<String> tokens = tokenizer.tokenize(null);

        Assertions.assertTrue(tokens.isEmpty());
    }

    @Test
    public void testSimpleText(){
        Set<String> tokens = tokenizer.tokenize("My name is Nikita");

        Assertions.assertEquals(4, tokens.size());
    }

    @Test
    public void testTextWithPunctuationMarks(){
        Set<String> tokens = tokenizer.tokenize("Hello, my name is Nikita! What is your name?");

        Assertions.assertEquals(7, tokens.size());
    }

    @Test
    public void testSlavicTextWithPunctuationMarks(){
        Set<String> tokens = tokenizer.tokenize("Было бы неплохо пройти на стажировку. Как считаете, я прав?");

        Assertions.assertEquals(10, tokens.size());
    }
}