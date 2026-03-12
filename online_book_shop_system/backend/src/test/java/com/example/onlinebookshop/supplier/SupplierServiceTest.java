package com.example.onlinebookshop.supplier;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Supplier Service Tests")
class SupplierServiceTest {

    @Mock
    private SupplierRepository repo;

    @InjectMocks
    private SupplierService service;

    @Test
    @DisplayName("getAll returns list from repo")
    void getAll() {
        SupplierDTO dto = new SupplierDTO();
        dto.setId(1L);
        when(repo.findAll()).thenReturn(List.of(dto));
        assertEquals(1, service.getAll().size());
    }

    @Test
    @DisplayName("getById found → returns DTO")
    void getById_found() {
        SupplierDTO dto = new SupplierDTO();
        dto.setId(1L);
        dto.setName("Test");
        when(repo.findById(1L)).thenReturn(dto);
        assertEquals("Test", service.getById(1L).getName());
    }

    @Test
    @DisplayName("getById not found → throws")
    void getById_notFound() {
        when(repo.findById(999L)).thenReturn(null);
        assertThrows(RuntimeException.class, () -> service.getById(999L));
    }

    @Test
    @DisplayName("create → inserts and returns")
    void create_success() {
        CreateSupplierRequest req = new CreateSupplierRequest();
        req.setName("New");
        when(repo.insert(req)).thenReturn(10L);
        SupplierDTO dto = new SupplierDTO();
        dto.setId(10L);
        when(repo.findById(10L)).thenReturn(dto);

        assertEquals(10L, service.create(req).getId());
    }

    @Test
    @DisplayName("update found → updates and returns")
    void update_found() {
        UpdateSupplierRequest req = new UpdateSupplierRequest();
        when(repo.update(1L, req)).thenReturn(1);
        SupplierDTO dto = new SupplierDTO();
        dto.setId(1L);
        when(repo.findById(1L)).thenReturn(dto);

        assertEquals(1L, service.update(1L, req).getId());
    }

    @Test
    @DisplayName("update not found → throws")
    void update_notFound() {
        UpdateSupplierRequest req = new UpdateSupplierRequest();
        when(repo.update(999L, req)).thenReturn(0);
        assertThrows(RuntimeException.class, () -> service.update(999L, req));
    }

    @Test
    @DisplayName("delete found → success")
    void delete_found() {
        when(repo.softDelete(1L)).thenReturn(1);
        assertDoesNotThrow(() -> service.delete(1L));
    }

    @Test
    @DisplayName("delete not found → throws")
    void delete_notFound() {
        when(repo.softDelete(999L)).thenReturn(0);
        assertThrows(RuntimeException.class, () -> service.delete(999L));
    }
}
