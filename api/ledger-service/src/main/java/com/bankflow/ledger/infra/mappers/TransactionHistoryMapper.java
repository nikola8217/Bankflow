package com.bankflow.ledger.infra.mappers;

import com.bankflow.ledger.core.entities.TransactionHistory;
import com.bankflow.ledger.infra.models.TransactionHistoryModel;
import org.springframework.stereotype.Component;

@Component
public class TransactionHistoryMapper {

    public TransactionHistoryModel toModel(TransactionHistory history) {
        TransactionHistoryModel model = new TransactionHistoryModel();
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

    public TransactionHistory toDomain(TransactionHistoryModel model) {
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