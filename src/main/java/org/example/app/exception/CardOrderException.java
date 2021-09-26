package org.example.app.exception;

public class CardOrderException extends RuntimeException {
    public CardOrderException() {
    }

    public CardOrderException(String message) {
        super(message);
    }

    public CardOrderException(String message, Throwable cause) {
        super(message, cause);
    }

    public CardOrderException(Throwable cause) {
        super(cause);
    }

    public CardOrderException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
