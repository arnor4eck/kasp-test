package com.arnor4eck.index;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class FullWordsTokenizer implements Tokenizer {

    private static final Pattern W_SPACES_AND_PUNCTUATION_MARKS = Pattern.compile("(?U)[\\p{L}]+");;

    @Override
    public Set<String> tokenize(String text) {
        if(text == null || text.trim().isEmpty())
            return Collections.emptySet();

        Set<String> tokens = new LinkedHashSet<>();

        Matcher matches = W_SPACES_AND_PUNCTUATION_MARKS.matcher(text);
        while (matches.find()) {
            tokens.add(matches.group());
        }

        return tokens;
    }
}
