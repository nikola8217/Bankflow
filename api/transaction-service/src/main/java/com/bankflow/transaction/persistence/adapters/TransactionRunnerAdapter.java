package com.bankflow.transaction.persistence.adapters;

import com.bankflow.transaction.application.ports.TransactionRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@Component
public class TransactionRunnerAdapter implements TransactionRunner {

    private final TransactionTemplate transactionTemplate;

    public TransactionRunnerAdapter(PlatformTransactionManager transactionManager) {
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    @Override
    public void inTransaction(Runnable work) {
        transactionTemplate.executeWithoutResult(status -> work.run());
    }
}