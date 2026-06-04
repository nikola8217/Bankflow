package com.bankflow.ledger.business.projections;

import com.bankflow.ledger.business.ports.IBalanceRepository;
import com.bankflow.ledger.core.entities.Balance;
import com.bankflow.shared.enums.TransactionType;
import com.bankflow.shared.events.TransactionCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Slf4j
@Component
@RequiredArgsConstructor
public class BalanceProjection {

    private final IBalanceRepository balanceRepository;

    public void handle(TransactionCreatedEvent event) {
        Balance balance = balanceRepository
                .findByAccountId(event.accountId())
                .orElse(new Balance(event.accountId(), BigDecimal.ZERO, event.currency()));

        if (event.type() == TransactionType.DEPOSIT) {
            balance.credit(event.amount());
        } else if (event.type() == TransactionType.WITHDRAWAL) {
            balance.debit(event.amount());
        } else if (event.type() == TransactionType.TRANSFER) {
            balance.debit(event.amount());
        }

        balanceRepository.save(balance);

        log.info("Balance updated for account: {}", event.accountId());
    }
}