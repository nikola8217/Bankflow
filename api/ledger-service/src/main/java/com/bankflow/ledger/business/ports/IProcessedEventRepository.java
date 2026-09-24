package com.bankflow.ledger.business.ports;

public interface IProcessedEventRepository {
    boolean exists(String key);
    void save(String key);
}