package com.bankflow.transaction.business.services;

import com.bankflow.transaction.business.ports.IEventStore;
import com.bankflow.transaction.business.responses.TransactionStatusResponse;
import com.bankflow.transaction.core.exceptions.TransactionNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionQueryService {

    private final IEventStore eventStore;

    public TransactionStatusResponse getTransaction(UUID transactionId, UUID currentUserId) {
        return eventStore.load(transactionId)
                .filter(transaction -> currentUserId.equals(transaction.getUserId()))
                .map(TransactionStatusResponse::from)
                .orElseThrow(() -> new TransactionNotFoundException(transactionId));
    }
}