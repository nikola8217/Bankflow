package com.bankflow.transaction.core.events;

import com.bankflow.shared.enums.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@AllArgsConstructor
public class TransactionInitiatedEvent {
    private UUID transactionId;
    private UUID accountId;
    private UUID userId;
    private TransactionType type;
    private BigDecimal amount;
    private String currency;
    private UUID targetAccountId;
}