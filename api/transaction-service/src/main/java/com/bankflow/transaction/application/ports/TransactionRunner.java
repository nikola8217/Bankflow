package com.bankflow.transaction.application.ports;

public interface TransactionRunner {
    void inTransaction(Runnable work);
}