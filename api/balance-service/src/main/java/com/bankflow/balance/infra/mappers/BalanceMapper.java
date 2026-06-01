package com.bankflow.balance.infra.mappers;

import com.bankflow.balance.core.entities.Balance;
import com.bankflow.balance.infra.models.BalanceModel;
import org.springframework.stereotype.Component;

@Component
public class BalanceMapper {

    public BalanceModel toNewModel(Balance balance) {
        BalanceModel model = new BalanceModel();
        model.setAccountId(balance.getAccountId());
        model.setAmount(balance.getAmount());
        model.setCurrency(balance.getCurrency());
        model.setUpdatedAt(balance.getUpdatedAt());
        return model;
    }

    public BalanceModel toExistingModel(Balance balance) {
        BalanceModel model = toNewModel(balance);
        model.setId(balance.getId());
        return model;
    }

    public Balance toDomain(BalanceModel model) {
        return new Balance(
                model.getId(),
                model.getAccountId(),
                model.getAmount(),
                model.getCurrency(),
                model.getUpdatedAt()
        );
    }
}