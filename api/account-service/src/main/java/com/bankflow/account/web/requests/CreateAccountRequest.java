package com.bankflow.account.web.requests;

import com.bankflow.account.application.dtos.CreateAccount;
import com.bankflow.account.domain.enums.AccountType;
import com.bankflow.account.domain.enums.Currency;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
public class CreateAccountRequest {

    @NotNull(message = "Account type is required")
    private AccountType type;

    @NotNull(message = "Currency is required")
    private Currency currency;

    public CreateAccount format(UUID userId) {
        return new CreateAccount(
                userId,
                type,
                currency
        );
    }
}