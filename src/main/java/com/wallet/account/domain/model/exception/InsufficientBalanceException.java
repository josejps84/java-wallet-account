package com.wallet.account.domain.model.exception;

/**
 * InsufficientBalanceException
 */
public class InsufficientBalanceException extends RuntimeException {

    public InsufficientBalanceException(String message) {
        super(message);
    }

}
