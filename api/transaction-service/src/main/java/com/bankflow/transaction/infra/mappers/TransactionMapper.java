package com.bankflow.transaction.infra.mappers;

import com.bankflow.transaction.core.valueObjects.AccountSnapshot;
import com.bankflow.transaction.core.entities.Transaction;
import com.bankflow.transaction.infra.models.TransactionModel;
import org.springframework.stereotype.Component;

@Component
public class TransactionMapper {

    public TransactionModel toModel(Transaction transaction) {
        TransactionModel model = new TransactionModel();
        model.setUserId(transaction.getUserId());
        model.setAccountId(transaction.getAccountSnapshot().id());
        model.setType(transaction.getType());
        model.setAmount(transaction.getAmount());
        model.setCurrency(transaction.getAccountSnapshot().currency());
        model.setAccountType(transaction.getAccountSnapshot().type());
        model.setStatus(transaction.getStatus());
        model.setTargetAccountId(transaction.getTargetAccountId());
        model.setCreatedAt(transaction.getCreatedAt());
        model.setUpdatedAt(transaction.getUpdatedAt());
        return model;
    }

    public Transaction toDomain(TransactionModel model) {
        AccountSnapshot snapshot = new AccountSnapshot(
                model.getAccountId(),
                model.getAccountType(),
                model.getCurrency(),
                "ACTIVE"
        );

        return new Transaction(
                model.getId(),
                model.getUserId(),
                model.getType(),
                model.getAmount(),
                snapshot,
                model.getStatus(),
                model.getTargetAccountId(),
                model.getCreatedAt(),
                model.getUpdatedAt()
        );
    }
}