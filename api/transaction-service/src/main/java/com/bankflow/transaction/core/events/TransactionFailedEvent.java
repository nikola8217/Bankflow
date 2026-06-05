package com.bankflow.transaction.core.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransactionFailedEvent {
    private UUID transactionId;
    private String reason;
    private LocalDateTime failedAt;
}