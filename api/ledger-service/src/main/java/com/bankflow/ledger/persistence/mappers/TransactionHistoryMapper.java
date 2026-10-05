package com.bankflow.ledger.persistence.mappers;

import com.bankflow.ledger.domain.models.TransactionHistory;
import com.bankflow.ledger.persistence.jpa.TransactionHistoryJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class TransactionHistoryMapper {

    public TransactionHistoryJpaEntity toModel(TransactionHistory history) {
        TransactionHistoryJpaEntity model = new TransactionHistoryJpaEntity();
        model.setTransactionId(history.getTransactionId());
        model.setAccountId(history.getAccountId());
        model.setUserId(history.getUserId());
        model.setType(history.getType());
        model.setAmount(history.getAmount());
        model.setCurrency(history.getCurrency());
        model.setTargetAccountId(history.getTargetAccountId());
        model.setStatus(history.getStatus());
        model.setCreatedAt(history.getCreatedAt());
        model.setFailureReason(history.getFailureReason());
        return model;
    }

    public TransactionHistory toDomain(TransactionHistoryJpaEntity model) {
        return new TransactionHistory(
                model.getTransactionId(),
                model.getAccountId(),
                model.getUserId(),
                model.getType(),
                model.getAmount(),
                model.getCurrency(),
                model.getTargetAccountId(),
                model.getStatus(),
                model.getCreatedAt(),
                model.getFailureReason()
        );
    }
}