package com.arnor4eck.index;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ToLowerCaseNormalizerTest {

    private final Normalizer toLowerCaseNormalizer = new ToLowerCaseNormalizer();

    @Test
    public void testNullToken(){
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> toLowerCaseNormalizer.normalize(null));
    }

    @Test
    public void testToLowerCaseToken(){
        String word = "HeLLO lETS CHeck tHIs NORMALIZer";
        String normalized = toLowerCaseNormalizer.normalize(word);

        Assertions.assertEquals("hello lets check this normalizer", normalized);
    }

    @Test
    public void testToLowerCaseWithSpacesToken(){
        String word = "    HeLLO \n";
        String normalized = toLowerCaseNormalizer.normalize(word);

        Assertions.assertEquals("hello", normalized);
    }
}