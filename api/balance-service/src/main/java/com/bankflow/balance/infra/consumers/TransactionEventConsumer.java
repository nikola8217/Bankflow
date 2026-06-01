package com.bankflow.balance.infra.consumers;

import com.bankflow.balance.business.services.BalanceService;
import com.bankflow.shared.events.TransactionCreatedEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class TransactionEventConsumer {

    private final BalanceService balanceService;

    public TransactionEventConsumer(BalanceService balanceService) {
        this.balanceService = balanceService;
    }

    @KafkaListener(topics = "transaction-created", groupId = "balance-service")
    public void consume(TransactionCreatedEvent event) {
        balanceService.processTransaction(event);
    }
}