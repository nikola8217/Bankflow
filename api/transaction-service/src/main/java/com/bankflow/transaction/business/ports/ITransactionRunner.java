package com.bankflow.transaction.business.ports;

public interface ITransactionRunner {
    void inTransaction(Runnable work);
}