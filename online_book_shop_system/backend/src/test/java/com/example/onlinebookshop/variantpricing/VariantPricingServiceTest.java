package com.example.onlinebookshop.variantpricing;

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
@DisplayName("Variant & Pricing Service Tests")
class VariantPricingServiceTest {

    @Mock
    private VariantPricingRepository repo;

    @InjectMocks
    private VariantPricingService service;

    @Test
    @DisplayName("getAllVariants → delegates to repo.findAllVariants()")
    void getAllVariants() {
        when(repo.findAllVariants()).thenReturn(List.of(new VariantDTO()));
        assertEquals(1, service.getAllVariants().size());
        verify(repo).findAllVariants();
    }

    @Test
    @DisplayName("getVariantsByBook → delegates to repo.findVariantsByBookId()")
    void getVariantsByBook() {
        when(repo.findVariantsByBookId(5L)).thenReturn(List.of());
        assertTrue(service.getVariantsByBook(5L).isEmpty());
        verify(repo).findVariantsByBookId(5L);
    }

    @Test
    @DisplayName("getVariantById found → returns DTO")
    void getVariantById_found() {
        VariantDTO dto = new VariantDTO();
        dto.setId(1L);
        when(repo.findVariantById(1L)).thenReturn(dto);
        assertEquals(1L, service.getVariantById(1L).getId());
    }

    @Test
    @DisplayName("getVariantById not found → throws RuntimeException")
    void getVariantById_notFound() {
        when(repo.findVariantById(999L)).thenReturn(null);
        assertThrows(RuntimeException.class, () -> service.getVariantById(999L));
    }

    @Test
    @DisplayName("createVariant valid → calls repo.insertVariant + findVariantById")
    void createVariant_valid() {
        CreateVariantRequest req = new CreateVariantRequest();
        req.setBookId(1L);
        req.setSku("SKU-001");
        when(repo.insertVariant(req)).thenReturn(10L);

        VariantDTO created = new VariantDTO();
        created.setId(10L);
        when(repo.findVariantById(10L)).thenReturn(created);

        assertEquals(10L, service.createVariant(req).getId());
        verify(repo).insertVariant(req);
    }

    @Test
    @DisplayName("createVariant missing bookId → throws IllegalArgumentException")
    void createVariant_missingBookId() {
        CreateVariantRequest req = new CreateVariantRequest();
        req.setSku("SKU-001");
        assertThrows(IllegalArgumentException.class, () -> service.createVariant(req));
    }

    @Test
    @DisplayName("createVariant missing SKU → throws IllegalArgumentException")
    void createVariant_missingSku() {
        CreateVariantRequest req = new CreateVariantRequest();
        req.setBookId(1L);
        assertThrows(IllegalArgumentException.class, () -> service.createVariant(req));
    }

    @Test
    @DisplayName("updateVariant found → calls repo.updateVariant + findVariantById")
    void updateVariant_found() {
        VariantDTO dto = new VariantDTO();
        dto.setId(1L);
        when(repo.findVariantById(1L)).thenReturn(dto);
        UpdateVariantRequest req = new UpdateVariantRequest();
        when(repo.updateVariant(1L, req)).thenReturn(1);

        assertNotNull(service.updateVariant(1L, req));
        verify(repo).updateVariant(1L, req);
    }

    @Test
    @DisplayName("updateVariant not found → throws RuntimeException")
    void updateVariant_notFound() {
        when(repo.findVariantById(999L)).thenReturn(null);
        assertThrows(RuntimeException.class,
                () -> service.updateVariant(999L, new UpdateVariantRequest()));
    }

    @Test
    @DisplayName("deleteVariant found → calls repo.softDeleteVariant")
    void deleteVariant_found() {
        when(repo.softDeleteVariant(1L)).thenReturn(1);
        assertDoesNotThrow(() -> service.deleteVariant(1L));
        verify(repo).softDeleteVariant(1L);
    }

    @Test
    @DisplayName("deleteVariant not found → throws RuntimeException")
    void deleteVariant_notFound() {
        when(repo.softDeleteVariant(999L)).thenReturn(0);
        assertThrows(RuntimeException.class, () -> service.deleteVariant(999L));
    }

    @Test
    @DisplayName("setBasePrice valid → calls repo.setBasePrice")
    void setBasePrice_valid() {
        VariantDTO dto = new VariantDTO();
        dto.setId(1L);
        when(repo.findVariantById(1L)).thenReturn(dto);

        VariantDTO result = service.setBasePrice(1L, 29.99, 24.99);
        verify(repo).setBasePrice(1L, 29.99, 24.99);
    }

    @Test
    @DisplayName("setBasePrice negative listPrice → throws IllegalArgumentException")
    void setBasePrice_negativeList() {
        VariantDTO dto = new VariantDTO();
        dto.setId(1L);
        when(repo.findVariantById(1L)).thenReturn(dto);

        assertThrows(IllegalArgumentException.class,
                () -> service.setBasePrice(1L, -1.0, 0.0));
    }

    @Test
    @DisplayName("setBasePrice null listPrice → throws IllegalArgumentException")
    void setBasePrice_nullList() {
        VariantDTO dto = new VariantDTO();
        dto.setId(1L);
        when(repo.findVariantById(1L)).thenReturn(dto);

        assertThrows(IllegalArgumentException.class,
                () -> service.setBasePrice(1L, null, 10.0));
    }

    @Test
    @DisplayName("setBasePrice negative salePrice → throws IllegalArgumentException")
    void setBasePrice_negativeSale() {
        VariantDTO dto = new VariantDTO();
        dto.setId(1L);
        when(repo.findVariantById(1L)).thenReturn(dto);

        assertThrows(IllegalArgumentException.class,
                () -> service.setBasePrice(1L, 10.0, -5.0));
    }

    @Test
    @DisplayName("setConditionPrices → calls repo.setConditionPrices")
    void setConditionPrices() {
        VariantDTO dto = new VariantDTO();
        dto.setId(1L);
        when(repo.findVariantById(1L)).thenReturn(dto);

        service.setConditionPrices(1L, "{\"LIKE_NEW\":{\"pct\":10}}");
        verify(repo).setConditionPrices(1L, "{\"LIKE_NEW\":{\"pct\":10}}");
    }

    @Test
    @DisplayName("getCopiesByVariant → delegates to repo.findCopiesByVariantId")
    void getCopiesByVariant() {
        when(repo.findCopiesByVariantId(1L)).thenReturn(List.of());
        assertTrue(service.getCopiesByVariant(1L).isEmpty());
        verify(repo).findCopiesByVariantId(1L);
    }

    @Test
    @DisplayName("overrideCopyPrice found → calls repo.overrideCopyPrice")
    void overrideCopyPrice_found() {
        when(repo.overrideCopyPrice(100L, 49.99)).thenReturn(1);
        assertDoesNotThrow(() -> service.overrideCopyPrice(100L, 49.99));
    }

    @Test
    @DisplayName("overrideCopyPrice not found → throws RuntimeException")
    void overrideCopyPrice_notFound() {
        when(repo.overrideCopyPrice(999L, 10.0)).thenReturn(0);
        assertThrows(RuntimeException.class, () -> service.overrideCopyPrice(999L, 10.0));
    }
}
