package com.bankflow.transaction.business.ports;

public interface IIdempotencyRepository {
    boolean exists(String idempotencyKey);
    void save(String idempotencyKey);
}