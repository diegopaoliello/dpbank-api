package com.dpbank.api.service.exception;

import java.util.UUID;

/**
 * Thrown when the provided account identifier does not correspond to a stored
 * account and requirement 5 (friendly messaging) must be honored.
 */
public class AccountNotFoundException extends LocalizedMessageException {

    /**
     * Builds the exception with the i18n message key and account identifier param.
     */
    public AccountNotFoundException(UUID accountId) {
        super("error.account.notFound", accountId);
    }
}
