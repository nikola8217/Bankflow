package com.bankflow.ledger.application.ports;

public interface ProcessedEventRepository {
    boolean exists(String key);
    void save(String key);
}