package com.dpbank.api.service.exception;

/**
 * Raised when a debit would drive the balance below zero. Controllers map this
 * exception to HTTP 422 so consumers can react programmatically.
 */
public class InsufficientBalanceException extends LocalizedMessageException {

    /**
     * Accepts the localized message key and optional args for message formatting.
     */
    public InsufficientBalanceException(String messageKey, Object... messageArgs) {
        super(messageKey, messageArgs);
    }
}
