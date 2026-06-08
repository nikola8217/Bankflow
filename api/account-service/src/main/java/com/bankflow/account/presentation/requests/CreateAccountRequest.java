package com.bankflow.account.presentation.requests;

import com.bankflow.account.business.dtos.CreateAccountDto;
import com.bankflow.account.core.enums.AccountType;
import com.bankflow.account.core.enums.Currency;
import com.bankflow.account.presentation.httpValidations.AccountRequestValidation;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
public class CreateAccountRequest {
    private AccountType type;
    private Currency currency;

    public CreateAccountDto format(UUID userId) {
        AccountRequestValidation.validateCreateAccount(this);

        return new CreateAccountDto(
                userId,
                type,
                currency
        );
    }
}
