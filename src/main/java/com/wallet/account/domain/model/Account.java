package com.wallet.account.domain.model;

import java.time.Instant;

public record Account(String email, double balance, Instant createdAt, Instant lastUpdatedAt) {

    public Account(String email, double balance) {
        this(email, balance, Instant.now(), Instant.now());
    }

    public Account(String email) {
        this(email, 0, Instant.now(), Instant.now());
    }

    public Account deposit(double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Deposit amount must be positive");
        }
        return new Account(email, balance + amount, createdAt, Instant.now());
    }

    public Account withdraw(double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Withdraw amount must be positive");
        }
        if (amount > balance) {
            throw new IllegalArgumentException("Insufficient balance");
        }
        return new Account(email, balance - amount, createdAt, Instant.now());
    }
    
}
