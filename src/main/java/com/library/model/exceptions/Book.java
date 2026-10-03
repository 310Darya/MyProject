package com.library.model;

import java.util.Objects;


public class Book {
    private final String isbn;
    private final String title;
    private final Author author;
    private final Genre genre;
    private final int publicationYear;

    public Book(String isbn, String title, Author author, Genre genre, int publicationYear) {
        this.isbn = isbn;
        this.title = title;
        this.author = author;
        this.genre = genre;
        this.publicationYear = publicationYear;
    }

    // Геттеры
    public String getIsbn() { return isbn; }
    public String getTitle() { return title; }
    public Author getAuthor() { return author; }
    public Genre getGenre() { return genre; }
    public int getPublicationYear() { return publicationYear; }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Book book)) return false;
        return Objects.equals(isbn, book.isbn);
    }

    @Override
    public int hashCode() {
        return Objects.hash(isbn);
    }

    @Override
    public String toString() {
        return String.format("[%s] %s (%d)", isbn, title, publicationYear);
    }
}