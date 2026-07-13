package com.cqutcm.biomed.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MobileCollectionServiceTest {
    @Mock
    private JdbcTemplate jdbc;
    @Mock
    private MobileRecordIngestionService ingestionService;

    private MobileCollectionService service;

    @BeforeEach
    void setUp() {
        service = new MobileCollectionService(jdbc, new PermissionService(), ingestionService);
    }

    @Test
    void disabledDeviceCannotUpload() {
        when(jdbc.queryForList(anyString(), anyString())).thenReturn(List.of(device("disabled")));

        assertThrows(AuthorizationDeniedException.class,
                () -> service.ingest("device-token", List.of(Map.of("clientRecordId", "record-1"))));
    }

    @Test
    void acceptedRecordIsReturnedAsDuplicate() {
        when(jdbc.queryForList(anyString(), anyString())).thenReturn(List.of(device("active")));
        when(ingestionService.claim("device-1", "record-1"))
                .thenReturn(new MobileRecordIngestionService.Claim(false, "accepted", "growth-1"));

        Map<String, Object> response = service.ingest(
                "device-token", List.of(Map.of("clientRecordId", "record-1")));

        assertEquals(0, response.get("accepted"));
        assertEquals(1, response.get("duplicates"));
        Map<?, ?> item = (Map<?, ?>) ((List<?>) response.get("items")).getFirst();
        assertEquals("duplicate", item.get("status"));
        assertEquals("growth-1", item.get("detail"));
    }

    @Test
    void rejectedItemDoesNotRollbackRemainingBatch() {
        when(jdbc.queryForList(anyString(), anyString())).thenReturn(List.of(device("active")));
        when(ingestionService.claim(eq("device-1"), anyString()))
                .thenReturn(MobileRecordIngestionService.Claim.acquired());
        when(ingestionService.accept(eq(device("active")), eq(Map.of("clientRecordId", "record-1")),
                eq("record-1"))).thenThrow(new IllegalArgumentException("invalid latitude"));
        when(ingestionService.accept(eq(device("active")), eq(Map.of("clientRecordId", "record-2")),
                eq("record-2"))).thenReturn("growth-2");

        Map<String, Object> response = service.ingest("device-token", List.of(
                Map.of("clientRecordId", "record-1"),
                Map.of("clientRecordId", "record-2")
        ));

        assertEquals(1, response.get("accepted"));
        assertEquals(1, response.get("rejected"));
        verify(ingestionService).reject("device-1", "record-1", "invalid latitude");
        verify(ingestionService).touchDevice("device-1");
    }

    @Test
    void rejectsOversizedBatchBeforeClaimingRecords() {
        when(jdbc.queryForList(anyString(), anyString())).thenReturn(List.of(device("active")));
        List<Map<String, Object>> records = java.util.stream.IntStream.range(0, 201)
                .mapToObj(index -> Map.<String, Object>of("clientRecordId", "record-" + index))
                .toList();

        assertThrows(IllegalArgumentException.class, () -> service.ingest("device-token", records));
    }

    private Map<String, Object> device(String status) {
        return Map.of(
                "id", "device-1",
                "device_code", "APP-001",
                "owner_name", "collector",
                "status", status
        );
    }
}
