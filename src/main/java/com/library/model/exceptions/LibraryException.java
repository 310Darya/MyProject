package com.library.model.exceptions;

public abstract class LibraryException extends RuntimeException {
    protected LibraryException(String message) {
        super(message);
    }
}