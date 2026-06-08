package com.bankflow.transaction.presentation.httpValidations;

import com.bankflow.shared.exceptions.ValidationException;
import com.bankflow.transaction.business.dtos.AccountTransactionDto;
import com.bankflow.transaction.business.dtos.TransferDto;
import com.bankflow.transaction.presentation.requests.AccountTransactionRequest;
import com.bankflow.transaction.presentation.requests.TransferRequest;

public class TransactionRequestsValidation {

    public static void validateAmount(AccountTransactionRequest request) {
        if (request.getAccountId() == null) {
            throw new ValidationException("Account id is required");
        }
        if (request.getAmount() == null || request.getAmount().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Amount must be greater than zero");
        }
    }

    public static void validateTransferRequest(TransferRequest request) {
        if (request.getFromAccountId() == null) {
            throw new ValidationException("From account id is required");
        }

        if (request.getToAccountId() == null) {
            throw new ValidationException("To account id is required");
        }

        if (request.getAmount() == null || request.getAmount().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Amount must be greater than zero");
        }
    }
}