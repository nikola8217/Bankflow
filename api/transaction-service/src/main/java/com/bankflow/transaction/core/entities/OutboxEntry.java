package com.bankflow.transaction.core.entities;

import com.bankflow.transaction.core.enums.OutboxStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class OutboxEntry {
    private UUID id;
    private UUID aggregateId;
    private String eventType;
    private Object payload;
    private OutboxStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime processedAt;
}