package com.bankflow.account.application.services;

import com.bankflow.account.application.dtos.CreateAccount;
import com.bankflow.account.application.ports.AccountEventPublisher;
import com.bankflow.account.application.ports.AccountRepository;
import com.bankflow.account.application.dtos.AccountResponse;
import com.bankflow.account.domain.models.Account;
import com.bankflow.account.domain.enums.AccountStatus;
import com.bankflow.account.domain.exceptions.AccountNotFoundException;
import com.bankflow.shared.events.AccountClosureDecidedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class AccountService {
    private final AccountRepository accountRepository;
    private final AccountEventPublisher eventPublisher;

    public AccountService(AccountRepository accountRepository, AccountEventPublisher eventPublisher) {
        this.accountRepository = accountRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public AccountResponse createAccount(CreateAccount dto) {
        Account account = new Account(
                UUID.randomUUID(),
                dto.userId(),
                dto.type(),
                AccountStatus.ACTIVE,
                dto.currency()
        );

        Account saved = accountRepository.create(account);

        eventPublisher.accountCreated(saved);

        return AccountResponse.from(saved);
    }

    public List<AccountResponse> getUserAccounts(UUID userId) {
        return accountRepository.findAllByUserId(userId)
                .stream()
                .map(AccountResponse::from)
                .toList();
    }

    public AccountResponse getAccount(UUID id, UUID currentUserId) {
        return AccountResponse.from(findOwned(id, currentUserId));
    }

    /**
     * Step one of the closing saga: the account becomes CLOSING and the Ledger is asked whether it can be closed.
     * The answer arrives later as an {@link AccountClosureDecidedEvent}. Asking again while closing changes nothing.
     */
    @Transactional
    public AccountResponse closeAccount(UUID id, UUID currentUserId) {
        Account account = findOwned(id, currentUserId);

        if (account.isClosing()) {
            return AccountResponse.from(account);
        }

        account.requestClose();

        Account saved = accountRepository.update(account);

        eventPublisher.closeRequested(saved);

        return AccountResponse.from(saved);
    }

    /**
     * Step three of the closing saga: apply the Ledger's decision. Approved, the account is closed and everyone is
     * told; rejected, it goes back to ACTIVE (the compensating step). A decision for an account that is no longer
     * closing is a redelivery and is ignored.
     */
    @Transactional
    public void applyClosureDecision(AccountClosureDecidedEvent decision) {
        Account account = accountRepository.findById(decision.accountId()).orElse(null);

        if (account == null || !account.isClosing()) {
            log.info("Ignoring closure decision for account {}: it is not closing", decision.accountId());

            return;
        }

        if (decision.approved()) {
            account.confirmClose();

            eventPublisher.accountClosed(accountRepository.update(account));

            log.info("Account closed: {}", account.getId());
        } else {
            account.cancelClose();

            accountRepository.update(account);

            log.info("Account closure rejected: {} ({})", account.getId(), decision.reason());
        }
    }

    private Account findOwned(UUID id, UUID currentUserId) {
        return accountRepository.findById(id)
                .filter(account -> account.getUserId().equals(currentUserId))
                .orElseThrow(() -> new AccountNotFoundException(id));
    }
}