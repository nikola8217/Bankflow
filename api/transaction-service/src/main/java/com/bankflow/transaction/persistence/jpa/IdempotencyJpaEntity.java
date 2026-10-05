package com.bankflow.transaction.persistence.jpa;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "idempotency_keys",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_idempotency_user_key",
                columnNames = {"userId", "idempotencyKey"}
        )
)
@Data
@NoArgsConstructor
public class IdempotencyJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private String idempotencyKey;

    @Column(nullable = false, length = 500)
    private String requestFingerprint;

    @Column(nullable = false)
    private UUID transactionId;

    @Column(nullable = false)
    private LocalDateTime createdAt;
}