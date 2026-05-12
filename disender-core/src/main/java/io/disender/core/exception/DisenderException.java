package io.disender.core.exception;

public class DisenderException extends RuntimeException {

    public DisenderException(String message) {
        super(message);
    }

    public DisenderException(String message, Throwable cause) {
        super(message, cause);
    }
}