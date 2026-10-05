package com.bankflow.transaction.persistence.jpa;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "transaction_events",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_transaction_events_aggregate_version",
                columnNames = {"aggregateId", "version"}
        )
)
@Data
@NoArgsConstructor
public class TransactionEventJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID aggregateId;

    @Column(nullable = false)
    private String aggregateType;

    @Column(nullable = false)
    private String eventType;

    @Column(nullable = false)
    private Integer version;

    @Column(nullable = false, columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private String payload;

    @Column(nullable = false)
    private LocalDateTime occurredAt;
}