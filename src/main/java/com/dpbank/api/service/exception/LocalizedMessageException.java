package com.dpbank.api.service.exception;

/**
 * Base runtime exception that keeps track of a message key plus optional
 * arguments so {@link org.springframework.context.MessageSource} can resolve
 * localized error payloads transparently.
 */
public abstract class LocalizedMessageException extends RuntimeException {

    private final String messageKey;
    private final transient Object[] messageArgs;

    /**
     * Captures the i18n key and dynamic values used to format the final message.
     */
    protected LocalizedMessageException(String messageKey, Object... messageArgs) {
        super(messageKey);
        this.messageKey = messageKey;
        this.messageArgs = messageArgs;
    }

    /**
     * @return the code used to resolve the localized message
     */
    public String getMessageKey() {
        return messageKey;
    }

    /**
     * @return the dynamic arguments associated with the localized message
     */
    public Object[] getMessageArgs() {
        return messageArgs;
    }
}
