package com.bankflow.transaction.core.events;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
public class TransactionCompletedEvent {
    private UUID transactionId;
    private LocalDateTime completedAt;
}