package com.example.onlinebookshop.bookmanagement;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Book Management Service Tests")
class BookManagementServiceTest {

    @Mock
    private BookManagementRepository repo;

    @InjectMocks
    private BookManagementService service;

    @Test
    @DisplayName("getAllBooks → delegates to repo.findAllBooks()")
    void getAllBooks_returnsListFromRepo() {
        BookListItemDTO dto = new BookListItemDTO();
        dto.setId(1L);
        when(repo.findAllBooks()).thenReturn(List.of(dto));

        List<BookListItemDTO> result = service.getAllBooks();
        assertEquals(1, result.size());
        verify(repo).findAllBooks();
    }

    @Test
    @DisplayName("getBookById found → returns DTO from repo.findBookById()")
    void getBookById_found() {
        BookDetailDTO dto = new BookDetailDTO();
        dto.setId(1L);
        dto.setTitle("Test Book");
        when(repo.findBookById(1L)).thenReturn(dto);

        BookDetailDTO result = service.getBookById(1L);
        assertEquals("Test Book", result.getTitle());
        verify(repo).findBookById(1L);
    }

    @Test
    @DisplayName("getBookById not found → throws RuntimeException")
    void getBookById_notFound_throws() {
        when(repo.findBookById(999L)).thenReturn(null);
        assertThrows(RuntimeException.class, () -> service.getBookById(999L));
    }

    @Test
    @DisplayName("createBook → calls repo.insertBook + repo.insertBookAuthors + repo.findBookById")
    void createBook_callsRepoInsert() {
        CreateBookRequest req = new CreateBookRequest();
        req.setTitle("New Book");
        req.setAuthors(List.of());
        req.setImages(List.of());

        when(repo.insertBook(req)).thenReturn(10L);
        BookDetailDTO detail = new BookDetailDTO();
        detail.setId(10L);
        when(repo.findBookById(10L)).thenReturn(detail);

        BookDetailDTO result = service.createBook(req);
        assertEquals(10L, result.getId());
        verify(repo).insertBook(req);
        verify(repo).insertBookAuthors(eq(10L), any());
        verify(repo).insertImages(eq(10L), any());
    }

    @Test
    @DisplayName("updateBook found → calls repo.updateBook then findBookById")
    void updateBook_found() {
        UpdateBookRequest req = new UpdateBookRequest();
        BookDetailDTO existing = new BookDetailDTO();
        existing.setId(1L);
        when(repo.findBookById(1L)).thenReturn(existing);

        BookDetailDTO result = service.updateBook(1L, req);
        verify(repo).updateBook(1L, req);
        verify(repo, atLeast(2)).findBookById(1L);
    }

    @Test
    @DisplayName("updateBook not found → throws RuntimeException")
    void updateBook_notFound() {
        when(repo.findBookById(999L)).thenReturn(null);
        assertThrows(RuntimeException.class,
                () -> service.updateBook(999L, new UpdateBookRequest()));
    }

    @Test
    @DisplayName("changeStatus HIDDEN → calls repo.changeStatus")
    void changeStatus_valid() {
        when(repo.changeStatus(1L, "HIDDEN")).thenReturn(1);
        assertDoesNotThrow(() -> service.changeStatus(1L, "HIDDEN"));
        verify(repo).changeStatus(1L, "HIDDEN");
    }

    @Test
    @DisplayName("changeStatus ACTIVE → calls repo.changeStatus")
    void changeStatus_active() {
        when(repo.changeStatus(1L, "ACTIVE")).thenReturn(1);
        assertDoesNotThrow(() -> service.changeStatus(1L, "ACTIVE"));
    }

    @Test
    @DisplayName("changeStatus DRAFT → calls repo.changeStatus")
    void changeStatus_draft() {
        when(repo.changeStatus(1L, "DRAFT")).thenReturn(1);
        assertDoesNotThrow(() -> service.changeStatus(1L, "DRAFT"));
    }

    @Test
    @DisplayName("changeStatus INVALID → throws IllegalArgumentException")
    void changeStatus_invalid_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> service.changeStatus(1L, "INVALID_STATUS"));
    }

    @Test
    @DisplayName("changeStatus valid but 0 rows → throws RuntimeException")
    void changeStatus_bookNotFound() {
        when(repo.changeStatus(999L, "ACTIVE")).thenReturn(0);
        assertThrows(RuntimeException.class,
                () -> service.changeStatus(999L, "ACTIVE"));
    }

    @Test
    @DisplayName("deleteBook success → calls repo.softDelete")
    void deleteBook_success() {
        when(repo.softDelete(1L)).thenReturn(1);
        assertDoesNotThrow(() -> service.deleteBook(1L));
        verify(repo).softDelete(1L);
    }

    @Test
    @DisplayName("deleteBook not found → throws RuntimeException")
    void deleteBook_notFound_throws() {
        when(repo.softDelete(999L)).thenReturn(0);
        assertThrows(RuntimeException.class, () -> service.deleteBook(999L));
    }

    @Test
    @DisplayName("getAllCategories → delegates to repo.findAllCategories")
    void getAllCategories_delegatesToRepo() {
        when(repo.findAllCategories()).thenReturn(List.of(Map.of("id", 1, "name", "Fiction")));
        List<Map<String, Object>> result = service.getAllCategories();
        assertFalse(result.isEmpty());
        verify(repo).findAllCategories();
    }

    @Test
    @DisplayName("getAllAuthors → delegates to repo.findAllAuthors")
    void getAllAuthors_delegatesToRepo() {
        when(repo.findAllAuthors()).thenReturn(List.of(Map.of("id", 1, "name", "Author")));
        List<Map<String, Object>> result = service.getAllAuthors();
        assertFalse(result.isEmpty());
        verify(repo).findAllAuthors();
    }
}
