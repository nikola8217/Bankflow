package com.bankflow.transaction.business.services;

import com.bankflow.transaction.business.dtos.AccountTransactionDto;
import com.bankflow.transaction.business.dtos.TransferDto;
import com.bankflow.transaction.business.ports.IAccountClient;
import com.bankflow.transaction.business.ports.ITransactionRepository;
import com.bankflow.transaction.business.responses.TransactionResponse;
import com.bankflow.transaction.core.entities.Transaction;
import com.bankflow.transaction.core.enums.TransactionStatus;
import com.bankflow.transaction.core.enums.TransactionType;
import com.bankflow.transaction.core.exceptions.AccountConflictException;
import com.bankflow.transaction.core.exceptions.AccountNotActiveException;
import com.bankflow.transaction.core.exceptions.InsufficientFundsException;
import com.bankflow.transaction.core.valueObjects.AccountSnapshot;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class TransactionService {
    private final ITransactionRepository transactionRepository;
    private final IAccountClient accountClient;

    public TransactionService(ITransactionRepository transactionRepository, IAccountClient accountClient) {
        this.transactionRepository = transactionRepository;
        this.accountClient = accountClient;
    }

    public TransactionResponse deposit(AccountTransactionDto dto, UUID userId, String token) {
        AccountSnapshot account = getAccountSnapshot(dto.accountId(), token);

        Transaction transaction = new Transaction(
                UUID.randomUUID(),
                userId,
                TransactionType.DEPOSIT,
                dto.amount(),
                account,
                TransactionStatus.PENDING,
                null,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        transaction.complete();

        return TransactionResponse.from(transactionRepository.save(transaction));
    }

    public TransactionResponse withdraw(AccountTransactionDto dto, UUID userId, String token) {
        AccountSnapshot account = getAccountSnapshot(dto.accountId(), token);

        BigDecimal balance = getBalance(dto.accountId());

        if (balance.compareTo(dto.amount()) < 0) {
            throw new InsufficientFundsException();
        }

        Transaction transaction = new Transaction(
                UUID.randomUUID(),
                userId,
                TransactionType.WITHDRAWAL,
                dto.amount(),
                account,
                TransactionStatus.PENDING,
                null,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        transaction.complete();

        return TransactionResponse.from(transactionRepository.save(transaction));
    }

    private AccountSnapshot getAccountSnapshot(UUID accountId, String token) {
        AccountSnapshot account = accountClient.getAccount(accountId, token);

        if (!account.status().equals("ACTIVE")) {
            throw new AccountNotActiveException(accountId);
        }

        return account;
    }

    public TransactionResponse transfer(TransferDto dto, UUID userId, String token) {
        if (dto.fromAccountId() == dto.toAccountId()) throw new AccountConflictException();

        AccountSnapshot fromAccount = getAccountSnapshot(dto.fromAccountId(), token);

        getAccountSnapshot(dto.toAccountId(), token);

        BigDecimal balance = getBalance(dto.fromAccountId());

        if (balance.compareTo(dto.amount()) < 0) {
            throw new InsufficientFundsException();
        }

        Transaction transaction = new Transaction(
                UUID.randomUUID(),
                userId,
                TransactionType.TRANSFER,
                dto.amount(),
                fromAccount,
                TransactionStatus.PENDING,
                dto.toAccountId(),
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        transaction.complete();

        return TransactionResponse.from(transactionRepository.save(transaction));
    }

    public List<TransactionResponse> getTransactionHistory(UUID accountId) {
        return transactionRepository.findAllByAccountId(accountId)
                .stream()
                .map(TransactionResponse::from)
                .toList();
    }

    private BigDecimal getBalance(UUID accountId) {
        List<Transaction> transactions = transactionRepository.findAllByAccountId(accountId);

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
