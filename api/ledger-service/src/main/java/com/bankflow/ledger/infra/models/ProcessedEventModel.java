package com.bankflow.ledger.infra.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "processed_events")
@Data
@NoArgsConstructor
public class ProcessedEventModel {

    @Id
    private UUID transactionId;

    @Column(nullable = false)
    private LocalDateTime processedAt;
}