package com.example.onlinebookshop.allocation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Allocation Service Tests")
class AllocationServiceTest {

    @Mock
    private AllocationRepository repo;

    @InjectMocks
    private AllocationService service;

    @Test
    @DisplayName("getSettings → delegates to repo.getSettings()")
    void getSettings() {
        AllocationSettingsDTO dto = new AllocationSettingsDTO("LOT", 30, "NEWEST_FIRST", false);
        when(repo.getSettings()).thenReturn(dto);

        AllocationSettingsDTO result = service.getSettings();
        assertEquals("LOT", result.getFifoBy());
        assertEquals(30, result.getReservationTtlMin());
        verify(repo).getSettings();
    }

    @Test
    @DisplayName("updateSettings → calls repo.saveSettings + repo.getSettings")
    void updateSettings() {
        AllocationSettingsDTO dto = new AllocationSettingsDTO("COPY", 60, "OLDEST_FIRST", true);
        when(repo.getSettings()).thenReturn(dto);

        AllocationSettingsDTO result = service.updateSettings(dto);
        verify(repo).saveSettings(dto);
        verify(repo).getSettings();
        assertEquals("COPY", result.getFifoBy());
    }
}
