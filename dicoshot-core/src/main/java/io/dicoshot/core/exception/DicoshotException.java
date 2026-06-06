package io.dicoshot.core.exception;

public class DicoshotException extends RuntimeException {

    public DicoshotException(String message) {
        super(message);
    }

    public DicoshotException(String message, Throwable cause) {
        super(message, cause);
    }
}