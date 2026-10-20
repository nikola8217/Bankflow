package com.bankflow.shared.events;

import java.util.UUID;

/**
 * The Ledger's answer to an {@link AccountCloseRequestedEvent}. When approved, the Ledger has already stopped
 * booking money on the account; when rejected, {@code reason} says why (for example a balance that is not zero).
 */
public record AccountClosureDecidedEvent(
        UUID accountId,
        boolean approved,
        String reason
) {

    public static AccountClosureDecidedEvent approved(UUID accountId) {
        return new AccountClosureDecidedEvent(accountId, true, null);
    }

    public static AccountClosureDecidedEvent rejected(UUID accountId, String reason) {
        return new AccountClosureDecidedEvent(accountId, false, reason);
    }
}