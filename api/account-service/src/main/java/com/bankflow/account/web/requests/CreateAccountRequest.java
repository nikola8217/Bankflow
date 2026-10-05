package com.bankflow.account.web.requests;

import com.bankflow.account.application.dtos.CreateAccount;
import com.bankflow.account.domain.enums.AccountType;
import com.bankflow.account.domain.enums.Currency;
import com.bankflow.account.web.requests.validations.AccountRequestValidation;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
public class CreateAccountRequest {
    private AccountType type;
    private Currency currency;

    public CreateAccount format(UUID userId) {
        AccountRequestValidation.validateCreateAccount(this);

        return new CreateAccount(
                userId,
                type,
                currency
        );
    }
}
