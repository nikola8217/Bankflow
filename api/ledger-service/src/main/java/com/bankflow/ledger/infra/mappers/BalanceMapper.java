package com.bankflow.ledger.infra.mappers;

import com.bankflow.ledger.core.entities.Balance;
import com.bankflow.ledger.infra.models.BalanceModel;
import org.springframework.stereotype.Component;

@Component
public class BalanceMapper {

    public BalanceModel toModel(Balance balance) {
        BalanceModel model = new BalanceModel();
        model.setAccountId(balance.getAccountId());
        model.setAmount(balance.getAmount());
        model.setCurrency(balance.getCurrency());
        model.setUpdatedAt(balance.getUpdatedAt());
        return model;
    }

    public Balance toDomain(BalanceModel model) {
        return new Balance(
                model.getAccountId(),
                model.getAmount(),
                model.getCurrency()
        );
    }
}