package com.cqutcm.biomed.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MobileRecordIngestionServiceTest {
    @Mock
    private JdbcTemplate jdbc;

    private MobileRecordIngestionService service;

    @BeforeEach
    void setUp() {
        service = new MobileRecordIngestionService(jdbc);
    }

    @Test
    void rejectedClientRecordCanBeClaimedAgain() {
        when(jdbc.queryForList(anyString(), eq("device-1"), eq("record-1"))).thenReturn(List.of(Map.of(
                "sync_status", "rejected",
                "error_message", "invalid latitude"
        )));
        when(jdbc.update(anyString(), eq("device-1"), eq("record-1"))).thenReturn(1);

        MobileRecordIngestionService.Claim claim = service.claim("device-1", "record-1");

        assertTrue(claim.claimed());
        assertEquals("processing", claim.status());
    }

    @Test
    void acceptedClientRecordKeepsOriginalGrowthRecordId() {
        when(jdbc.queryForList(anyString(), eq("device-1"), eq("record-1"))).thenReturn(List.of(Map.of(
                "sync_status", "accepted",
                "growth_record_id", "growth-1"
        )));

        MobileRecordIngestionService.Claim claim = service.claim("device-1", "record-1");

        assertEquals(false, claim.claimed());
        assertEquals("accepted", claim.status());
        assertEquals("growth-1", claim.detail());
    }

    @Test
    void rejectsIncompleteCoordinatesBeforeWritingGrowthRecord() {
        when(jdbc.queryForList(anyString(), eq("batch-1"))).thenReturn(List.of(Map.of(
                "id", "batch-1",
                "herb_name", "黄连",
                "district", "石柱县"
        )));

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class, () -> service.accept(
                device(),
                Map.of(
                        "batchId", "batch-1",
                        "clientRecordId", "record-1",
                        "recordedAt", LocalDateTime.now().toString(),
                        "latitude", 29.9
                ),
                "record-1"
        ));

        assertEquals("longitude and latitude must be provided together", error.getMessage());
    }

    @Test
    void rejectsFutureCollectionTime() {
        when(jdbc.queryForList(anyString(), eq("batch-1"))).thenReturn(List.of(Map.of(
                "id", "batch-1",
                "herb_name", "黄连",
                "district", "石柱县"
        )));

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class, () -> service.accept(
                device(),
                Map.of(
                        "batchId", "batch-1",
                        "recordedAt", LocalDateTime.now().plusHours(1).toString()
                ),
                "record-1"
        ));

        assertEquals("recordedAt cannot be more than 10 minutes in the future", error.getMessage());
    }

    private Map<String, Object> device() {
        return Map.of("id", "device-1", "owner_name", "collector");
    }
}
