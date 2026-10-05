package com.bankflow.transaction.domain.exceptions;

import com.bankflow.shared.exceptions.AppException;
import com.bankflow.shared.exceptions.ErrorType;

import java.util.UUID;

public class TransactionNotFoundException extends AppException {
    public TransactionNotFoundException(UUID id) {
        super("Transaction not found with id: " + id, ErrorType.NOT_FOUND);
    }
}