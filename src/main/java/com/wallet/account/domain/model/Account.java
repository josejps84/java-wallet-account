package com.wallet.account.domain.model;
import com.wallet.account.domain.model.exception.InsufficientBalanceException;

import lombok.extern.slf4j.Slf4j;

import java.time.Instant;

@Slf4j
public record Account(String email, double balance, Instant createdAt, Instant lastUpdatedAt) {

    public Account(String email, double balance) {
        this(email, balance, Instant.now(), Instant.now());
    }

    public Account(String email) {
        this(email, 0, Instant.now(), Instant.now());
    }

    public Account deposit(double amount) {
        if (amount <= 0) {
            log.error("Attempted to deposit a non-positive amount: {} in account {}", amount, email);
            throw new IllegalArgumentException("Deposit amount must be positive");
        }
        return new Account(email, balance + amount, createdAt, Instant.now());
    }

    public Account withdraw(double amount) {
        if (amount <= 0) {
            log.error("Attempted to withdraw a non-positive amount: {} from account {}", amount, email);
            throw new IllegalArgumentException("Withdraw amount must be positive");
        }
        if (amount > balance) {
            log.error("Attempted to withdraw {} from account {} with insufficient balance. Current balance: {}", amount, email, balance);
            throw new InsufficientBalanceException("Insufficient balance");
        }
        return new Account(email, balance - amount, createdAt, Instant.now());
    }
    
}
