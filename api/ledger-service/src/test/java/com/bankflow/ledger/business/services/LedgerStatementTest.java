package com.bankflow.ledger.business.services;

import com.bankflow.ledger.AbstractIntegrationTest;
import com.bankflow.ledger.business.ports.ITransactionHistoryRepository;
import com.bankflow.ledger.core.entities.TransactionHistory;
import com.bankflow.shared.enums.TransactionStatus;
import com.bankflow.shared.enums.TransactionType;
import com.bankflow.shared.events.AccountCreatedEvent;
import com.bankflow.shared.events.TransactionCreatedEvent;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class LedgerStatementTest extends AbstractIntegrationTest {

    @Autowired
    LedgerService ledgerService;

    @Autowired
    ITransactionHistoryRepository historyRepository;

    @Test
    void recipientSeesIncomingTransfer() {
        UUID anasAccount = newAccount();
        UUID jelenasAccount = newAccount();
        ledgerService.process(event(anasAccount, TransactionType.DEPOSIT, "1000.00", null));

        TransactionCreatedEvent transfer = event(anasAccount, TransactionType.TRANSFER, "300.00", jelenasAccount);
        ledgerService.process(transfer);

        List<TransactionHistory> jelenasStatement = historyRepository.findAllByAccountId(jelenasAccount);
        assertThat(jelenasStatement)
                .extracting(TransactionHistory::getTransactionId)
                .containsExactly(transfer.transactionId());

        assertThat(historyRepository.findAllByAccountId(anasAccount)).hasSize(2);
    }

    @Test
    void recipientDoesNotSeeDeclinedTransfer() {
        UUID anasAccount = newAccount();
        UUID jelenasAccount = newAccount();

        ledgerService.process(event(anasAccount, TransactionType.TRANSFER, "5000.00", jelenasAccount));

        assertThat(historyRepository.findAllByAccountId(jelenasAccount)).isEmpty();

        assertThat(historyRepository.findAllByAccountId(anasAccount))
                .extracting(TransactionHistory::getStatus)
                .containsExactly(TransactionStatus.FAILED);
    }

    private UUID newAccount() {
        UUID accountId = UUID.randomUUID();
        ledgerService.registerAccount(new AccountCreatedEvent(accountId, UUID.randomUUID(), "RSD"));
        return accountId;
    }

    private TransactionCreatedEvent event(UUID accountId, TransactionType type, String amount, UUID target) {
        return new TransactionCreatedEvent(
                UUID.randomUUID(), accountId, UUID.randomUUID(),
                type, new BigDecimal(amount), "RSD", target, LocalDateTime.now());
    }
}