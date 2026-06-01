package com.bankflow.transaction.business.services;

import com.bankflow.transaction.business.dtos.AccountTransactionDto;
import com.bankflow.transaction.business.dtos.TransferDto;
import com.bankflow.transaction.business.helpers.BalanceCalculator;
import com.bankflow.transaction.business.ports.IAccountClient;
import com.bankflow.transaction.business.ports.IEventPublisher;
import com.bankflow.transaction.business.ports.ITransactionRepository;
import com.bankflow.transaction.business.responses.TransactionResponse;
import com.bankflow.transaction.core.entities.Transaction;
import com.bankflow.transaction.core.enums.TransactionStatus;
import com.bankflow.transaction.core.enums.TransactionType;
import com.bankflow.transaction.core.events.TransactionCreatedEvent;
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
    private final IEventPublisher eventPublisher;

    public TransactionService(
            ITransactionRepository transactionRepository,
            IAccountClient accountClient,
            IEventPublisher eventPublisher
    ) {
        this.transactionRepository = transactionRepository;
        this.accountClient = accountClient;
        this.eventPublisher = eventPublisher;
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

        return saveAndPublish(transaction);
    }

    public TransactionResponse withdraw(AccountTransactionDto dto, UUID userId, String token) {
        AccountSnapshot account = getAccountSnapshot(dto.accountId(), token);

        BigDecimal balance = BalanceCalculator.calculate(dto.accountId(), transactionRepository.findAllByAccountId(dto.accountId()));

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

        return saveAndPublish(transaction);
    }

    public TransactionResponse transfer(TransferDto dto, UUID userId, String token) {
        if (dto.fromAccountId() == dto.toAccountId()) throw new AccountConflictException();

        AccountSnapshot fromAccount = getAccountSnapshot(dto.fromAccountId(), token);

        getAccountSnapshot(dto.toAccountId(), token);

        BigDecimal balance = BalanceCalculator.calculate(dto.fromAccountId(), transactionRepository.findAllByAccountId(dto.fromAccountId()));

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

        return saveAndPublish(transaction);
    }

    private AccountSnapshot getAccountSnapshot(UUID accountId, String token) {
        AccountSnapshot account = accountClient.getAccount(accountId, token);

        if (!account.status().equals("ACTIVE")) {
            throw new AccountNotActiveException(accountId);
        }

        return account;
    }

    private TransactionResponse saveAndPublish(Transaction transaction) {
        transaction.complete();

        Transaction saved = transactionRepository.save(transaction);

        eventPublisher.publish(new TransactionCreatedEvent(
                saved.getId(),
                saved.getAccountSnapshot().id(),
                saved.getUserId(),
                saved.getType(),
                saved.getAmount(),
                saved.getAccountSnapshot().currency(),
                saved.getTargetAccountId(),
                saved.getCreatedAt()
        ));

        return TransactionResponse.from(saved);
    }

    public List<TransactionResponse> getTransactionHistory(UUID accountId) {
        return transactionRepository.findAllByAccountId(accountId)
                .stream()
                .map(TransactionResponse::from)
                .toList();
    }
}
