package com.bankflow.transaction.presentation.httpValidations;

import com.bankflow.shared.exceptions.ValidationException;
import com.bankflow.transaction.presentation.requests.httpValidations.TransactionRequestsValidation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TransactionRequestsValidationTest {

    @ParameterizedTest
    @ValueSource(strings = {"100", "100.5", "100.50", "100.500", "0.01"})
    void acceptsAmountsWithAtMostTwoDecimals(String amount) {
        assertThatCode(() -> TransactionRequestsValidation.validateMoney(new BigDecimal(amount)))
                .doesNotThrowAnyException();
    }

    @ParameterizedTest
    @ValueSource(strings = {"0.005", "100.123", "100.505"})
    void rejectsAmountsWithMoreThanTwoDecimals(String amount) {
        assertThatThrownBy(() -> TransactionRequestsValidation.validateMoney(new BigDecimal(amount)))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("decimal places");
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "-10", "0.00"})
    void rejectsZeroAndNegativeAmounts(String amount) {
        assertThatThrownBy(() -> TransactionRequestsValidation.validateMoney(new BigDecimal(amount)))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("greater than zero");
    }

    @Test
    void rejectsMissingAmount() {
        assertThatThrownBy(() -> TransactionRequestsValidation.validateMoney(null))
                .isInstanceOf(ValidationException.class);
    }
}