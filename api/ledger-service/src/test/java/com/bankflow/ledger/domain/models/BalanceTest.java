package com.bankflow.ledger.domain.models;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BalanceTest {

    private Balance balanceWith(String amount) {
        return new Balance(UUID.randomUUID(), UUID.randomUUID(), new BigDecimal(amount), "RSD");
    }

    @Test
    void creditIncreasesAmount() {
        Balance balance = balanceWith("100.00");

        balance.credit(new BigDecimal("50.00"));

        assertThat(balance.getAmount()).isEqualByComparingTo("150.00");
    }

    @Test
    void debitDecreasesAmount() {
        Balance balance = balanceWith("100.00");

        balance.debit(new BigDecimal("30.00"));

        assertThat(balance.getAmount()).isEqualByComparingTo("70.00");
    }

    @Test
    void debitOfWholeBalanceLeavesZero() {
        Balance balance = balanceWith("100.00");

        balance.debit(new BigDecimal("100.00"));

        assertThat(balance.getAmount()).isEqualByComparingTo("0");
    }

    @Test
    void debitMoreThanBalanceThrowsAndLeavesAmountUnchanged() {
        Balance balance = balanceWith("100.00");

        assertThatThrownBy(() -> balance.debit(new BigDecimal("100.01")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Insufficient funds");

        assertThat(balance.getAmount()).isEqualByComparingTo("100.00");
    }

    @Test
    void canDebitIsTrueUpToTheFullBalance() {
        Balance balance = balanceWith("100.00");

        assertThat(balance.canDebit(new BigDecimal("100.00"))).isTrue();
        assertThat(balance.canDebit(new BigDecimal("100.01"))).isFalse();
    }

    @Test
    void emptyAccountCanBeClosed() {
        Balance balance = balanceWith("0.00");

        balance.close();

        assertThat(balance.isClosed()).isTrue();
    }

    @Test
    void accountWithMoneyCannotBeClosed() {
        Balance balance = balanceWith("0.01");

        assertThat(balance.canClose()).isFalse();
        assertThatThrownBy(balance::close).isInstanceOf(IllegalStateException.class);
        assertThat(balance.isClosed()).isFalse();
    }
}