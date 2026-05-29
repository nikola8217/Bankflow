package com.bankflow.account.infra.mappers;

import com.bankflow.account.core.entities.Account;
import com.bankflow.account.infra.models.AccountModel;
import org.springframework.stereotype.Component;

@Component
public class AccountMapper {

    public AccountModel toNewModel(Account account) {
        AccountModel model = new AccountModel();
        model.setUserId(account.getUserId());
        model.setType(account.getType());
        model.setCurrency(account.getCurrency());
        model.setStatus(account.getStatus());
        model.setCreatedAt(account.getCreatedAt());
        model.setUpdatedAt(account.getUpdatedAt());
        return model;
    }

    public AccountModel toExistingModel(Account account) {
        AccountModel model = toNewModel(account);
        model.setId(account.getId());
        return model;
    }

    public Account toDomain(AccountModel model) {
        return new Account(
                model.getId(),
                model.getUserId(),
                model.getType(),
                model.getStatus(),
                model.getCurrency()
        );
    }
}