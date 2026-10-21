package com.bankflow.account.messaging;

import com.bankflow.account.application.services.AccountService;
import com.bankflow.shared.events.AccountClosureDecidedEvent;
import com.bankflow.shared.events.Topics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AccountClosureConsumer {

    private final AccountService accountService;

    @KafkaListener(topics = Topics.ACCOUNT_CLOSURE_DECIDED, groupId = "account-service")
    public void consume(AccountClosureDecidedEvent decision) {
        log.info("Received AccountClosureDecidedEvent for account: {} (approved: {})",
                decision.accountId(), decision.approved());

        accountService.applyClosureDecision(decision);
    }
}