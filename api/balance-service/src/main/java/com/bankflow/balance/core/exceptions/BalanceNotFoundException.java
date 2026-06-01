package com.bankflow.balance.core.exceptions;

import com.bankflow.shared.exceptions.AppException;
import org.springframework.http.HttpStatus;

import java.util.UUID;

public class BalanceNotFoundException extends AppException {
    public BalanceNotFoundException(UUID accountId) {
        super("Balance not found for account: " + accountId, HttpStatus.NOT_FOUND);
    }
}