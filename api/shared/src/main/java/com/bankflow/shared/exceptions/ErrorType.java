package com.bankflow.shared.exceptions;

public enum ErrorType {
    /** The input is malformed or fails validation. */
    INVALID_REQUEST,
    /** The request is valid, but a business rule forbids it (insufficient funds, closed account, ...). */
    BUSINESS_RULE,
    /** The caller is not authenticated or the credentials are wrong. */
    UNAUTHORIZED,
    /** The referenced resource does not exist or is not visible to the caller. */
    NOT_FOUND,
    /** The request clashes with the current state (duplicate, already processed, ...). */
    CONFLICT,
    /** The request is well-formed but cannot be applied as sent (e.g. an idempotency key reused for different data). */
    UNPROCESSABLE,
    /** A dependency (another service) is not available. */
    UNAVAILABLE
}