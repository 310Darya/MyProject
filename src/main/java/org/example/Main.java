package org.example;

import com.library.model.*;
import com.library.service.LibraryCatalog;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        System.out.println("БИБЛИОТЕКА\n");

        // 1. Создаем авторов
        var author1 = new Author("Лев", "Толстой");
        var author2 = new Author("Федор", "Достоевский");
        var author3 = new Author("Джордж", "Оруэлл");
        var author4 = new Author("Рэй", "Брэдбери");
        var author5 = new Author("Джоан", "Роулинг");

        // 2. Создаем книги (разные жанры)
        var book1 = new Book("978-1", "Война и мир", author1, Genre.FICTION, 1869);
        var book2 = new Book("978-2", "Преступление и наказание", author2, Genre.FICTION, 1866);
        var book3 = new Book("978-3", "1984", author3, Genre.SCIENCE_FICTION, 1949);
        var book4 = new Book("978-4", "451 градус по Фаренгейту", author4, Genre.SCIENCE_FICTION, 1953);
        var book5 = new Book("978-5", "Гарри Поттер", author5, Genre.FANTASY, 1997);
        var book6 = new Book("978-6", "История России", null, Genre.HISTORY, 2020);
        var book7 = new Book("978-7", "Java для начинающих", null, Genre.TECHNICAL, 2023);
        var book8 = new Book("978-8", "Шерлок Холмс", null, Genre.DETECTIVE, 1892);

        // 3. Создаем структура хранения (композиция)
        var row1 = new ShelfRow(1, 20);
        var row2 = new ShelfRow(2, 20);
        var shelfA = new Shelf("Шкаф А", List.of(row1, row2));
        var shelfB = new Shelf("Шкаф Б", List.of(new ShelfRow(1, 15)));
        var room1 = new Room("Читальный зал", List.of(shelfA, shelfB));

        var loc1 = new StorageLocation(room1, shelfA, row1);
        var loc2 = new StorageLocation(room1, shelfA, row2);
        var loc3 = new StorageLocation(room1, shelfB, new ShelfRow(1, 15));

        // 4.Создаем экземпляры книг
        var catalog = new LibraryCatalog();

        //доб. по несколько экземпляров каждой книги
        for (int i = 1; i <= 5; i++) {
            catalog.addCopy(new BookCopy("C-" + i, book1, loc1));
            catalog.addCopy(new BookCopy("C-" + (i+5), book2, loc1));
            catalog.addCopy(new BookCopy("C-" + (i+10), book3, loc2));
            catalog.addCopy(new BookCopy("C-" + (i+15), book4, loc2));
            catalog.addCopy(new BookCopy("C-" + (i+20), book5, loc3));
            catalog.addCopy(new BookCopy("C-" + (i+25), book6, loc3));
            catalog.addCopy(new BookCopy("C-" + (i+30), book7, loc1));
            catalog.addCopy(new BookCopy("C-" + (i+35), book8, loc2));
        }
        //8 книг * 5 экземпляров

        System.out.println("Добавлено 40 экземпляров книг.");

        // 5. Демонстрация поиска
        System.out.println("\nПоиск по автору (Толстой)");
        catalog.searchByAuthor(author1).forEach(c ->
                System.out.println("  Найдено: " + c + " на " + c.getLocation().getFullAddress()));

        System.out.println("\nПоиск по жанру (Фантастика)");
        catalog.searchByGenre(Genre.SCIENCE_FICTION).forEach(c ->
                System.out.println("  Найдено: " + c));

        //6. Демонстрация выдачи
        System.out.println("\nВыдача книги ");
        var now = Instant.now();
        var moscowZone = ZoneId.of("Europe/Moscow");
        var loan = new Loan("Иван Петров", now, Duration.ofDays(14), moscowZone);

        try {
            catalog.loanCopy("C-1", loan);
            System.out.println(" Книга C-1 выдана. Вернуть до: " + loan.formatDueDate());
        } catch (Exception e) {
            System.out.println("Ошибка: " + e.getMessage());
        }

        // 7. Проверка просрочки
        System.out.println("\nПросроченные книги ");
        var futureDate = now.plus(Duration.ofDays(20)); // Дата в будущем
        var overdue = catalog.getOverdueCopies(futureDate);
        if (overdue.isEmpty()) {
            System.out.println("  Нет просроченных книг.");
        } else {
            overdue.forEach(c -> System.out.println("  Просрочена: " + c));
        }

        // 8. Отчеты
        System.out.println("\n Отчет по жанрам ");
        catalog.getGenreDistribution().forEach((genre, count) ->
                System.out.println("  " + genre + ": " + count + " экз."));

        System.out.println("\nДЕМО ГОТОВО ");
    }
}