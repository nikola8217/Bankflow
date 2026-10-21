package com.bankflow.account.application.services;

import com.bankflow.account.application.ports.AccountEventPublisher;
import com.bankflow.account.domain.models.Account;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Remembers which events the service published, as "type:accountId", in order. */
class RecordingAccountEventPublisher implements AccountEventPublisher {

    final List<String> published = new ArrayList<>();

    @Override
    public void accountCreated(Account account) {
        published.add("created:" + account.getId());
    }

    @Override
    public void closeRequested(Account account) {
        published.add("closeRequested:" + account.getId());
    }

    @Override
    public void accountClosed(Account account) {
        published.add("closed:" + account.getId());
    }

    static String closeRequested(UUID accountId) {
        return "closeRequested:" + accountId;
    }

    static String closed(UUID accountId) {
        return "closed:" + accountId;
    }
}