package com.petstore.order.exception;

public class ServiceUnavailableException extends RuntimeException {
    public ServiceUnavailableException(String serviceName, String message) {
        super(String.format("Service '%s' is temporarily unavailable: %s", serviceName, message));
    }

    public ServiceUnavailableException(String message) {
        super(message);
    }
}
