package com.bankflow.ledger.business.services;

import com.bankflow.ledger.business.ports.IBalanceRepository;
import com.bankflow.ledger.business.ports.ITransactionHistoryRepository;
import com.bankflow.ledger.core.entities.Balance;
import com.bankflow.ledger.core.entities.TransactionHistory;
import com.bankflow.ledger.core.exceptions.BalanceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LedgerQueryService {

    private final IBalanceRepository balanceRepository;
    private final ITransactionHistoryRepository historyRepository;

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