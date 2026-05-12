package com.jacent.storefront.exception;

public class ResourceInundationException extends RuntimeException {
    public ResourceInundationException(String message) {
        super(message);
    }

    public ResourceInundationException(String message, Throwable cause) {
        super(message, cause);
    }
}