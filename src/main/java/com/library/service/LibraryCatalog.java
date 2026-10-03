package com.library.service;

import com.library.model.*;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Сервис для управления библиотекой.
 * Реализует интерфейс Searchable для демонстрации полиморфизма.
 */
public class LibraryCatalog implements Searchable {
    private final List<BookCopy> copies = new ArrayList<>();
    private final Map<String, List<Loan>> history = new HashMap<>(); // История по ISBN книги

    /**
     * Добавить экземпляр книги в каталог.
     * @param copy экземпляр книги для добавления
     */
    public void addCopy(BookCopy copy) {
        copies.add(copy);
    }

    // --- БАЗОВЫЙ УРОВЕНЬ (реализация интерфейса Searchable) ---

    /**
     * Поиск книг по автору.
     * @param author автор для поиска
     * @return список найденных экземпляров
     */
    @Override
    public List<BookCopy> searchByAuthor(Author author) {
        return copies.stream()
                .filter(c -> c.getBook().getAuthor() != null
                        && c.getBook().getAuthor().equals(author))
                .toList();
    }

    /**
     * Поиск книг по названию (частичное совпадение, без учета регистра).
     * @param keyword ключевое слово для поиска
     * @return список найденных экземпляров
     */
    @Override
    public List<BookCopy> searchByTitle(String keyword) {
        var lower = keyword.toLowerCase();
        return copies.stream()
                .filter(c -> c.getBook().getTitle().toLowerCase().contains(lower))
                .toList();
    }

    /**
     * Поиск книг по жанру.
     * @param genre жанр для поиска
     * @return список найденных экземпляров
     */
    @Override
    public List<BookCopy> searchByGenre(Genre genre) {
        return copies.stream()
                .filter(c -> c.getBook().getGenre() == genre)
                .toList();
    }

    // --- ДОПОЛНИТЕЛЬНЫЕ МЕТОДЫ ---

    /**
     * Получить полный адрес хранения экземпляра.
     * @param copyId ID экземпляра
     * @return строка с адресом или сообщение об отсутствии
     */
    public String whereIs(String copyId) {
        return copies.stream()
                .filter(c -> c.getId().equals(copyId))
                .findFirst()
                .map(c -> c.getLocation().getFullAddress())
                .orElse("Экземпляр не найден");
    }

    /**
     * Выдать книгу читателю.
     * @param copyId ID экземпляра
     * @param loan объект выдачи с датой и сроком
     * @throws com.library.model.exceptions.AlreadyLoanedException если книга уже выдана
     */
    public void loanCopy(String copyId, Loan loan) {
        var copy = findCopyOrThrow(copyId);
        copy.loan(loan);
        history.computeIfAbsent(copy.getBook().getIsbn(), k -> new ArrayList<>()).add(loan);
    }

    /**
     * Вернуть книгу в библиотеку.
     * @param copyId ID экземпляра
     * @throws com.library.model.exceptions.NotLoanedException если книга не была выдана
     */
    public void returnCopy(String copyId) {
        findCopyOrThrow(copyId).returnCopy();
    }

    /**
     * Получить список просроченных книг на указанную дату.
     * @param checkDate дата проверки
     * @return список просроченных экземпляров
     */
    public List<BookCopy> getOverdueCopies(Instant checkDate) {
        return copies.stream()
                .filter(c -> c.getState() == CopyState.LOANED
                        && c.getActiveLoan().isOverdue(checkDate))
                .toList();
    }

    /**
     * Получить все выданные книги.
     * @return список выданных экземпляров
     */
    public List<BookCopy> getAllLoaned() {
        return copies.stream()
                .filter(c -> c.getState() == CopyState.LOANED)
                .toList();
    }

    // --- ПОВЫШЕННЫЙ УРОВЕНЬ ---

    /**
     * Переместить все книги со старого шкафа в новое место.
     * @param oldShelf старый шкаф
     * @param newLocation новое место хранения
     */
    public void moveShelfContents(Shelf oldShelf, StorageLocation newLocation) {
        copies.stream()
                .filter(c -> c.getLocation().shelf().equals(oldShelf))
                .forEach(c -> c.setLocation(newLocation));
    }

    /**
     * Получить историю выдач конкретной книги по ISBN.
     * @param isbn ISBN книги
     * @return список записей о выдачах
     */
    public List<Loan> getHistoryByBook(String isbn) {
        return history.getOrDefault(isbn, List.of());
    }

    /**
     * Отчет: количество книг на каждой полке.
     * @return карта "адрес полки" -> "количество книг"
     */
    public Map<String, Long> getShelfOccupancyReport() {
        return copies.stream()
                .collect(Collectors.groupingBy(
                        c -> c.getLocation().getFullAddress(),
                        Collectors.counting()));
    }

    /**
     * Отчет: распределение книг по жанрам.
     * @return карта "жанр" -> "количество экземпляров"
     */
    public Map<Genre, Long> getGenreDistribution() {
        return copies.stream()
                .collect(Collectors.groupingBy(
                        c -> c.getBook().getGenre(),
                        Collectors.counting()));
    }

    // Вспомогательный метод
    private BookCopy findCopyOrThrow(String id) {
        return copies.stream()
                .filter(c -> c.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Экземпляр " + id + " не найден"));
    }
}