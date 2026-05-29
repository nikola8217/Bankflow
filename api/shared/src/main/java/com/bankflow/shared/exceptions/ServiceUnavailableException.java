package com.bankflow.shared.exceptions;

import org.springframework.http.HttpStatus;

public class ServiceUnavailableException extends AppException {
    public ServiceUnavailableException(String service) {
        super(service + " is currently unavailable", HttpStatus.SERVICE_UNAVAILABLE);
    }
}