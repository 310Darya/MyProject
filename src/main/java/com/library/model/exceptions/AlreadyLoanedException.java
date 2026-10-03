package com.library.model.exceptions;


public class AlreadyLoanedException extends LibraryException {
    public AlreadyLoanedException(String isbn) {
        super("Ошибка: Книга с ISBN " + isbn + " уже выдена другому читателю!");
    }
}