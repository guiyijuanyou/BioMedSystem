package com.cqutcm.biomed.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class StatusMachineTest {

    @Test
    void ownerCanSubmitDraftAndRejectedRecord() {
        assertDoesNotThrow(() -> StatusMachine.assertTransition(
                StatusMachine.DRAFT, StatusMachine.PENDING_REVIEW, "记录"));
        assertDoesNotThrow(() -> StatusMachine.assertTransition(
                StatusMachine.REJECTED, StatusMachine.PENDING_REVIEW, "记录"));
    }

    @Test
    void pendingRecordCanOnlyBeApprovedOrRejected() {
        assertDoesNotThrow(() -> StatusMachine.assertTransition(
                StatusMachine.PENDING_REVIEW, StatusMachine.APPROVED, "记录"));
        assertDoesNotThrow(() -> StatusMachine.assertTransition(
                StatusMachine.PENDING_REVIEW, StatusMachine.REJECTED, "记录"));
        assertThrows(StateConflictException.class, () -> StatusMachine.assertTransition(
                StatusMachine.PENDING_REVIEW, StatusMachine.PUBLISHED, "记录"));
    }

    @Test
    void onlyDraftAndRejectedRecordsAreOwnerEditable() {
        assertDoesNotThrow(() -> StatusMachine.assertOwnerEditable(StatusMachine.DRAFT, "记录"));
        assertDoesNotThrow(() -> StatusMachine.assertOwnerEditable(StatusMachine.REJECTED, "记录"));
        assertThrows(StateConflictException.class, () ->
                StatusMachine.assertOwnerEditable(StatusMachine.PENDING_REVIEW, "记录"));
        assertThrows(StateConflictException.class, () ->
                StatusMachine.assertOwnerEditable(StatusMachine.PUBLISHED, "记录"));
    }
}
