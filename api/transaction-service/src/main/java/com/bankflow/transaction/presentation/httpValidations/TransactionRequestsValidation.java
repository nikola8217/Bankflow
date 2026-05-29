package com.bankflow.transaction.presentation.httpValidations;

import com.bankflow.shared.exceptions.ValidationException;
import com.bankflow.transaction.business.dtos.AccountTransactionDto;
import com.bankflow.transaction.business.dtos.TransferDto;

public class TransactionRequestsValidation {

    public static void validateAmount(AccountTransactionDto dto) {
        if (dto.accountId() == null) {
            throw new ValidationException("Account id is required");
        }
        if (dto.amount() == null || dto.amount().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Amount must be greater than zero");
        }
    }

    public static void validateTransferRequest(TransferDto dto) {
        if (dto.fromAccountId() == null) {
            throw new ValidationException("From account id is required");
        }

        if (dto.toAccountId() == null) {
            throw new ValidationException("To account id is required");
        }

        if (dto.amount() == null || dto.amount().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Amount must be greater than zero");
        }
    }
}