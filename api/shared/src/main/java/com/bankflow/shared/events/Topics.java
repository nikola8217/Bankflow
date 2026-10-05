package com.bankflow.shared.events;

public final class Topics {

    public static final String ACCOUNT_CREATED = "account-created";
    public static final String TRANSACTION_CREATED = "transaction-created";
    public static final String TRANSACTION_APPROVED = "transaction-approved";
    public static final String TRANSACTION_DECLINED = "transaction-declined";

    public static final String DLT_SUFFIX = "-dlt";

    private Topics() {
    }
}