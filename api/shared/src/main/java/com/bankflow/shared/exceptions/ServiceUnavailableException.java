package com.bankflow.shared.exceptions;

public class ServiceUnavailableException extends AppException {
    public ServiceUnavailableException(String service) {
        super(service + " is currently unavailable", ErrorType.UNAVAILABLE);
    }
}