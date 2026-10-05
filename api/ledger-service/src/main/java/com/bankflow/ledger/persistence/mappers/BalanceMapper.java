package com.bankflow.ledger.persistence.mappers;

import com.bankflow.ledger.domain.models.Balance;
import com.bankflow.ledger.persistence.jpa.BalanceJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class BalanceMapper {

    public BalanceJpaEntity toModel(Balance balance) {
        BalanceJpaEntity model = new BalanceJpaEntity();
        model.setUserId(balance.getUserId());
        model.setAccountId(balance.getAccountId());
        model.setAmount(balance.getAmount());
        model.setCurrency(balance.getCurrency());
        model.setUpdatedAt(balance.getUpdatedAt());
        return model;
    }

    public Balance toDomain(BalanceJpaEntity model) {
        return Balance.restore(
                model.getUserId(),
                model.getAccountId(),
                model.getAmount(),
                model.getCurrency(),
                model.getUpdatedAt()
        );
    }
}