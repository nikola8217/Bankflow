package com.bankflow.account.persistence.mappers;

import com.bankflow.account.domain.models.Account;
import com.bankflow.account.persistence.jpa.AccountJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class AccountMapper {

    public AccountJpaEntity toModel(Account account) {
        AccountJpaEntity model = new AccountJpaEntity();
        model.setId(account.getId());
        model.setUserId(account.getUserId());
        model.setType(account.getType());
        model.setCurrency(account.getCurrency());
        model.setStatus(account.getStatus());
        model.setCreatedAt(account.getCreatedAt());
        model.setUpdatedAt(account.getUpdatedAt());
        return model;
    }

    public Account toDomain(AccountJpaEntity model) {
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