package com.bankflow.balance.business.services;

import com.bankflow.balance.business.ports.IBalanceRepository;
import com.bankflow.balance.business.responses.BalanceResponse;
import com.bankflow.balance.core.entities.Balance;
import com.bankflow.balance.core.exceptions.BalanceNotFoundException;
import com.bankflow.shared.events.TransactionCreatedEvent;
import com.bankflow.shared.enums.TransactionType;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class BalanceService {

    private final IBalanceRepository balanceRepository;

    public BalanceService(IBalanceRepository balanceRepository) {
        this.balanceRepository = balanceRepository;
    }

    public BalanceResponse getBalance(UUID accountId) {
        Balance balance = balanceRepository.findByAccountId(accountId)
                .orElseThrow(() -> new BalanceNotFoundException(accountId));
        return BalanceResponse.from(balance);
    }

    public void processTransaction(TransactionCreatedEvent event) {
        Optional<Balance> existing = balanceRepository.findByAccountId(event.accountId());

        if (existing.isEmpty()) {
            Balance balance = new Balance(
                    UUID.randomUUID(),
                    event.accountId(),
                    BigDecimal.ZERO,
                    event.currency(),
                    LocalDateTime.now()
            );

            if (event.type() == TransactionType.DEPOSIT) {
                balance.deposit(event.amount());
            } else if (event.type() == TransactionType.WITHDRAWAL) {
                balance.withdraw(event.amount());
            } else if (event.type() == TransactionType.TRANSFER) {
                balance.withdraw(event.amount());
            }

            balanceRepository.create(balance);
        } else {
            Balance balance = existing.get();

            if (event.type() == TransactionType.DEPOSIT) {
                balance.deposit(event.amount());
            } else if (event.type() == TransactionType.WITHDRAWAL) {
                balance.withdraw(event.amount());
            } else if (event.type() == TransactionType.TRANSFER) {
                balance.withdraw(event.amount());
                // target account balance update happens separately
            }

            balanceRepository.update(balance);
        }
    }
}