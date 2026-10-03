package com.library.model.exceptions;

public class NotLoanedException extends LibraryException {
    public NotLoanedException(String isbn) {
        super("Ошибка: Книга с ISBN " + isbn + " не находится в выдаче.");
    }
}