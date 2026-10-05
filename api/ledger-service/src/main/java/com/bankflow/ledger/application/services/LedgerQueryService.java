package com.bankflow.ledger.application.services;

import com.bankflow.ledger.application.ports.BalanceRepository;
import com.bankflow.ledger.application.ports.TransactionHistoryRepository;
import com.bankflow.ledger.domain.models.Balance;
import com.bankflow.ledger.domain.models.TransactionHistory;
import com.bankflow.ledger.domain.exceptions.BalanceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LedgerQueryService {

    private final BalanceRepository balanceRepository;
    private final TransactionHistoryRepository historyRepository;

    public Balance getBalance(UUID accountId, UUID currentUserId) {
        return balanceRepository.findByAccountId(accountId)
                .filter(balance -> currentUserId.equals(balance.getUserId()))
                .orElseThrow(() -> new BalanceNotFoundException(accountId));
    }

    public List<TransactionHistory> getHistory(UUID accountId, UUID currentUserId) {
        getBalance(accountId, currentUserId);
        return historyRepository.findAllByAccountId(accountId);
    }
}