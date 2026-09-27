package com.bankflow.account.infra.mappers;

import com.bankflow.account.core.entities.Account;
import com.bankflow.account.infra.models.AccountModel;
import org.springframework.stereotype.Component;

@Component
public class AccountMapper {

    public AccountModel toModel(Account account) {
        AccountModel model = new AccountModel();
        model.setId(account.getId());
        model.setUserId(account.getUserId());
        model.setType(account.getType());
        model.setCurrency(account.getCurrency());
        model.setStatus(account.getStatus());
        model.setCreatedAt(account.getCreatedAt());
        model.setUpdatedAt(account.getUpdatedAt());
        return model;
    }

    public Account toDomain(AccountModel model) {
        return Account.restore(
                model.getId(),
                model.getUserId(),
                model.getType(),
                model.getStatus(),
                model.getCurrency(),
                model.getCreatedAt(),
                model.getUpdatedAt()
        );
    }
}