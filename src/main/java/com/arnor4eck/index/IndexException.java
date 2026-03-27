package com.arnor4eck.index;

class IndexException extends RuntimeException {
    public IndexException(String message) {
        super(message);
    }

    public IndexException(String message, Throwable cause) {
        super(message, cause);
    }
}
