package com.bankflow.account.infra.repositories;

import com.bankflow.account.infra.models.AccountModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AccountJpaRepository extends JpaRepository<AccountModel, UUID> {
    List<AccountModel> findAllByUserId(UUID userId);
}