package com.library.model;

// ✅ ТРЕБОВАНИЕ: перечисление с полями и описанием
public enum Genre {
    FICTION("Художественная проза"),
    SCIENCE_FICTION("Научная фантастика"),
    FANTASY("Фэнтези"),
    DETECTIVE("Детектив и триллер"),
    HISTORY("История"),
    BIOGRAPHY("Биографии"),
    TECHNICAL("Техническая литература"),
    SCIENCE("Естественные науки");

    private final String description;

    Genre(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}