package com.bankflow.transaction.core.exceptions;

import com.bankflow.shared.exceptions.AppException;
import org.springframework.http.HttpStatus;

import java.util.UUID;

public class TransactionNotFoundException extends AppException {
    public TransactionNotFoundException(UUID id) {
        super("Transaction not found with id: " + id, HttpStatus.NOT_FOUND);
    }
}