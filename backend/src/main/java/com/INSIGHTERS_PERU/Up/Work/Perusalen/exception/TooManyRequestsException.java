package com.INSIGHTERS_PERU.Up.Work.Perusalen.exception;

public class TooManyRequestsException extends RuntimeException {
    public TooManyRequestsException(String message) {
        super(message);
    }
}
