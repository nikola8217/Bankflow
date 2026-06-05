package com.bankflow.transaction.core.aggregates;

import com.bankflow.shared.enums.TransactionType;
import com.bankflow.shared.enums.TransactionStatus;
import com.bankflow.transaction.core.events.TransactionCompletedEvent;
import com.bankflow.transaction.core.events.TransactionFailedEvent;
import com.bankflow.transaction.core.events.TransactionInitiatedEvent;
import com.bankflow.transaction.core.exceptions.TransactionException;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
public class TransactionAggregate {

    private UUID transactionId;
    private UUID accountId;
    private UUID userId;
    private TransactionType type;
    private BigDecimal amount;
    private String currency;
    private UUID targetAccountId;
    private TransactionStatus status;
    private int version;

    private final List<Object> domainEvents = new ArrayList<>();

    public TransactionAggregate() {}

    public void initiate(UUID transactionId, UUID accountId, UUID userId,
                         TransactionType type, BigDecimal amount,
                         String currency, UUID targetAccountId) {

        if (this.status != null) {
            throw new TransactionException("Transaction is already initiated", HttpStatus.CONFLICT);
        }

        TransactionInitiatedEvent event = new TransactionInitiatedEvent(
                transactionId, accountId, userId,
                type, amount, currency, targetAccountId
        );

        apply(event);
        domainEvents.add(event);
    }

    public void complete() {
        if (this.status != TransactionStatus.PENDING) {
            throw new TransactionException("Transaction " + transactionId + " is not in PENDING status", HttpStatus.BAD_REQUEST);
        }

        TransactionCompletedEvent event = new TransactionCompletedEvent(
                this.transactionId, LocalDateTime.now()
        );

        apply(event);
        domainEvents.add(event);
    }

    public void fail(String reason) {
        if (this.status == TransactionStatus.COMPLETED) {
            throw new TransactionException("Transaction " + transactionId + " is already completed", HttpStatus.CONFLICT);
        }

        TransactionFailedEvent event = new TransactionFailedEvent(
                this.transactionId, reason, LocalDateTime.now()
        );

        apply(event);
        domainEvents.add(event);
    }


    public void apply(TransactionInitiatedEvent event) {
        this.transactionId = event.getTransactionId();
        this.accountId = event.getAccountId();
        this.userId = event.getUserId();
        this.type = event.getType();
        this.amount = event.getAmount();
        this.currency = event.getCurrency();
        this.targetAccountId = event.getTargetAccountId();
        this.status = TransactionStatus.PENDING;
        this.version++;
    }

    public void apply(TransactionCompletedEvent event) {
        this.status = TransactionStatus.COMPLETED;
        this.version++;
    }

    public void apply(TransactionFailedEvent event) {
        this.status = TransactionStatus.FAILED;
        this.version++;
    }

    public List<Object> pullDomainEvents() {
        List<Object> events = new ArrayList<>(domainEvents);
        domainEvents.clear();
        return events;
    }
}