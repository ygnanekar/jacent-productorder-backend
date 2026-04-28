package com.jacent.storefront.exception;

public class ResourceNotFoundException extends RuntimeException  {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
