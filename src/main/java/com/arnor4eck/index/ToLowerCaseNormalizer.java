package com.arnor4eck.index;

final class ToLowerCaseNormalizer implements Normalizer {
    @Override
    public String normalize(String token) throws IllegalArgumentException {
        if(token == null)
            throw new IllegalArgumentException("Значение токена не может быть null.");
        return token.trim().toLowerCase();
    }
}
