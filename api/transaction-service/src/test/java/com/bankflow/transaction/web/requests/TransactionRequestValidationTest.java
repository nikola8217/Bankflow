package com.bankflow.transaction.web.requests;

import tools.jackson.databind.json.JsonMapper;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class TransactionRequestValidationTest {

    private static final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
    private static final JsonMapper json = JsonMapper.builder().build();

    private static Set<String> violations(Object request) {
        Set<ConstraintViolation<Object>> result = validator.validate(request);
        return result.stream().map(ConstraintViolation::getMessage).collect(Collectors.toSet());
    }

    private static AccountTransactionRequest deposit(String amount) throws Exception {
        return json.readValue(
                "{\"accountId\":\"" + UUID.randomUUID() + "\",\"amount\":" + amount + "}",
                AccountTransactionRequest.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"100", "100.5", "100.50", "0.01"})
    void acceptsAmountsWithAtMostTwoDecimals(String amount) throws Exception {
        assertThat(violations(deposit(amount))).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {"0.005", "100.123", "100.505"})
    void rejectsAmountsWithMoreThanTwoDecimals(String amount) throws Exception {
        assertThat(violations(deposit(amount))).contains("Amount can have at most 2 decimal places");
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "-10", "0.00"})
    void rejectsZeroAndNegativeAmounts(String amount) throws Exception {
        assertThat(violations(deposit(amount))).contains("Amount must be greater than zero");
    }

    @Test
    void rejectsMissingAmountAndAccount() throws Exception {
        AccountTransactionRequest request = json.readValue("{}", AccountTransactionRequest.class);

        assertThat(violations(request)).contains("Amount is required", "Account id is required");
    }

    @Test
    void rejectsTransferToTheSameAccount() throws Exception {
        UUID account = UUID.randomUUID();
        TransferRequest request = json.readValue(
                "{\"fromAccountId\":\"" + account + "\",\"toAccountId\":\"" + account + "\",\"amount\":10}",
                TransferRequest.class);

        assertThat(violations(request)).containsExactly("Cannot transfer to the same account");
    }
}