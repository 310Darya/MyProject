package com.library;

import com.library.model.*;
import com.library.model.exceptions.AlreadyLoanedException;
import com.library.model.exceptions.NotLoanedException;
import com.library.service.LibraryCatalog;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

//базовый функ, искл, гран.случаи
class LibraryCatalogTest {

    private LibraryCatalog catalog;
    private Book book;
    private Author author;
    private BookCopy copy1;
    private BookCopy copy2;
    private StorageLocation location;

    // запускается ПЕРЕД каждым тестом
    //создает "чистую" библиотеку с двумя книгами, чтобы каждый тест начинался с одинаковых условий
    @BeforeEach
    void setUp() {
        catalog = new LibraryCatalog();
        author = new Author("Лев", "Толстой");
        book = new Book("978-001", "Война и мир", author, Genre.FICTION, 1869);

        var room = new Room("Зал 1", List.of(new Shelf("Шкаф А", List.of(new ShelfRow(1, 10)))));
        var shelf = room.getShelves().get(0);
        var row = shelf.getRows().get(0);
        location = new StorageLocation(room, shelf, row);

        copy1 = new BookCopy("C-1", book, location);
        copy2 = new BookCopy("C-2", book, location);

        catalog.addCopy(copy1);
        catalog.addCopy(copy2);
    }



    // Проверка, что поиск по автору работает: если ищем Толстого, должны найти 2 его книги.
    @Test
    void testSearchByAuthorFound() {
        var results = catalog.searchByAuthor(author);
        assertEquals(2, results.size(), "Должно быть найдено 2 экземпляра");
    }

    // Проверяем обратную ситуацию: если ищем Достоевского (которого нет в базе), список должен быть пустым.
    @Test
    void testSearchByAuthorNotFound() {
        var otherAuthor = new Author("Федор", "Достоевский");
        var results = catalog.searchByAuthor(otherAuthor);
        assertTrue(results.isEmpty(), "По другому автору ничего не должно быть найдено");
    }

    // Проверяем частичный поиск по названию: слово "война" должно найти "Войну и мир".
    @Test
    void testSearchByTitlePartialMatch() {
        var results = catalog.searchByTitle("война");
        assertEquals(2, results.size());
    }

    // Проверяем, что поиск нечувствителен к регистру: "ВОЙНА" (заглавными) тоже должна найти книгу.
    @Test
    void testSearchByTitleCaseInsensitive() {
        var results = catalog.searchByTitle("ВОЙНА");
        assertEquals(2, results.size(), "Поиск должен быть без учета регистра");
    }

    // Проверяем поиск по жанру: если ищем FICTION, должны найти 2 книги этого жанра.
    @Test
    void testSearchByGenre() {
        var results = catalog.searchByGenre(Genre.FICTION);
        assertEquals(2, results.size());
    }

    // выдача, возврат

    // Проверяем успешную выдачу: после вызова loanCopy статус книги должен стать LOANED.
    @Test
    void testLoanCopySuccess() {
        var loan = new Loan("Иван", Instant.now(), Duration.ofDays(14), ZoneId.systemDefault());
        assertDoesNotThrow(() -> catalog.loanCopy("C-1", loan));
        assertEquals(CopyState.LOANED, copy1.getState());
    }

    // Проверяем исключение: если пытаемся выдать уже выданную книгу, программа должна выбросить AlreadyLoanedException.
    @Test
    void testLoanCopyAlreadyLoanedThrowsException() {
        var loan = new Loan("Иван", Instant.now(), Duration.ofDays(14), ZoneId.systemDefault());
        catalog.loanCopy("C-1", loan);

        // Попытка выдать ту же книгу снова
        assertThrows(AlreadyLoanedException.class, () -> catalog.loanCopy("C-1", loan));
    }

    // Проверяем успешный возврат: после returnCopy статус книги должен снова стать AVAILABLE.
    @Test
    void testReturnCopySuccess() {
        var loan = new Loan("Иван", Instant.now(), Duration.ofDays(14), ZoneId.systemDefault());
        catalog.loanCopy("C-1", loan);

        assertDoesNotThrow(() -> catalog.returnCopy("C-1"));
        assertEquals(CopyState.AVAILABLE, copy1.getState());
    }

    // Проверяем исключение при возврате: если книга не была выдана, возврат должен выбросить NotLoanedException.
    @Test
    void testReturnCopyNotLoanedThrowsException() {
        // Пытаемся вернуть книгу, которая не была выдана
        assertThrows(NotLoanedException.class, () -> catalog.returnCopy("C-1"));
    }

    //граничные случаи и equals/hashCode

    // Проверяем, где лежит книга: метод whereIs должен вернуть адрес с названием комнаты ("Зал 1").
    @Test
    void testWhereIsExistingCopy() {
        String address = catalog.whereIs("C-1");
        assertFalse(address.contains("не найден"), "Адрес должен быть найден");
        assertTrue(address.contains("Зал 1"), "Адрес должен содержать название комнаты");
    }

    // Проверяем несуществующую книгу: если спросить про ID "C-999", должно вернуться сообщение "не найден".
    @Test
    void testWhereIsNonExistingCopy() {
        String address = catalog.whereIs("C-999");
        assertEquals("Экземпляр не найден", address);
    }

    // Проверяем контракт equals/hashCode: две разные книги с ОДИНАКОВЫМ ISBN должны считаться равными.
    @Test
    void testBookEqualsAndHashCode() {
        var book2 = new Book("978-001", "Другое название", author, Genre.HISTORY, 2000);
        assertEquals(book, book2, "Книги с одинаковым ISBN должны быть равны");
        assertEquals(book.hashCode(), book2.hashCode(), "Хеш-коды равных книг должны совпадать");
    }

    // Проверяем detection просрочки: если взять книгу 20 дней назад на срок 14 дней, она должна быть в списке просроченных.
    @Test
    void testOverdueCopiesDetection() {
        var pastLoan = new Loan("Иван", Instant.now().minus(Duration.ofDays(20)),
                Duration.ofDays(14), ZoneId.systemDefault());
        catalog.loanCopy("C-1", pastLoan);

        var overdue = catalog.getOverdueCopies(Instant.now());
        assertEquals(1, overdue.size(), "Должна быть найдена 1 просроченная книга");
    }

    // Проверяем работу с пустым каталогом: поиск в новой пустой библиотеке не должен падать, а возвращать пустой список.
    @Test
    void testEmptyCatalogSearch() {
        var emptyCatalog = new LibraryCatalog();
        assertTrue(emptyCatalog.searchByAuthor(author).isEmpty());
        assertTrue(emptyCatalog.searchByTitle("test").isEmpty());
    }

    // Проверяем метод getAllLoaned: после выдачи одной книги список выданных должен содержать ровно 1 элемент.
    @Test
    void testGetAllLoaned() {
        var loan = new Loan("Иван", Instant.now(), Duration.ofDays(14), ZoneId.systemDefault());
        catalog.loanCopy("C-1", loan);

        var loaned = catalog.getAllLoaned();
        assertEquals(1, loaned.size(), "Должна быть найдена 1 выданная книга");
        assertEquals("C-1", loaned.get(0).getId());
    }
}