package org.example.app.exception;

public class ResetCodeNotMatchesException extends RuntimeException {
    public ResetCodeNotMatchesException() {
    }

    public ResetCodeNotMatchesException(String message) {
        super(message);
    }

    public ResetCodeNotMatchesException(String message, Throwable cause) {
        super(message, cause);
    }

    public ResetCodeNotMatchesException(Throwable cause) {
        super(cause);
    }

    public ResetCodeNotMatchesException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
