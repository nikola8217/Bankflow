package com.bankflow.ledger.core.entities;

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
}