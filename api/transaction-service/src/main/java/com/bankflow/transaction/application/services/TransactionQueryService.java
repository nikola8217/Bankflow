package com.bankflow.transaction.application.services;

import com.bankflow.transaction.application.ports.EventStore;
import com.bankflow.transaction.application.dtos.TransactionStatusResponse;
import com.bankflow.transaction.domain.exceptions.TransactionNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionQueryService {

    private final EventStore eventStore;

    public TransactionStatusResponse getTransaction(UUID transactionId, UUID currentUserId) {
        return eventStore.load(transactionId)
                .filter(transaction -> currentUserId.equals(transaction.getUserId()))
                .map(TransactionStatusResponse::from)
                .orElseThrow(() -> new TransactionNotFoundException(transactionId));
    }
}