package com.bankflow.transaction.business.helpers;

import com.bankflow.transaction.core.entities.Transaction;
import com.bankflow.transaction.core.enums.TransactionType;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public class BalanceCalculator {

    public static BigDecimal calculate(UUID accountId, List<Transaction> transactions) {
        BigDecimal balance = BigDecimal.ZERO;

        for (Transaction t : transactions) {
            switch (t.getType()) {
                case DEPOSIT -> balance = balance.add(t.getAmount());
                case WITHDRAWAL -> balance = balance.subtract(t.getAmount());
                case TRANSFER -> {
                    if (t.getAccountSnapshot().id().equals(accountId)) {
                        balance = balance.subtract(t.getAmount());
                    } else {
                        balance = balance.add(t.getAmount());
                    }
                }
            }
        }

        return balance;
    }
}