package com.wallet.account.domain.model.exception;

/**
 * AccountNotFoundException
 */
public class AccountNotFoundException extends RuntimeException {

    public AccountNotFoundException(String message) {
        super(message);
    }

}
