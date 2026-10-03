package com.library.model;

public record Author(String firstName, String lastName) {

    @Override
    public String toString() {
        return lastName + " " + firstName;
    }
}