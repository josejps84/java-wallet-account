package com.wallet.account.domain.model;

import com.wallet.account.domain.model.exception.InsufficientBalanceException;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.Instant;

@Slf4j
@Builder
public record Account(
        String accountNmbr,
        BigDecimal currentBalance,
        String currency,
        AccountStatus status,
        Long userId,
        Instant dateCreation,
        Instant lastUpdate
) {

    public Account(String accountNmbr, Long userId) {
        this(accountNmbr, BigDecimal.ZERO, "USD", AccountStatus.ACTIVE, userId, Instant.now(), Instant.now());
    }

    public Account deposit(double amount) {
        if (amount <= 0) {
            log.error("Attempted to deposit a non-positive amount: {} in account {}", amount, accountNmbr);
            throw new IllegalArgumentException("Deposit amount must be positive");
        }

        BigDecimal nextBalance = currentBalance.add(BigDecimal.valueOf(amount));
        return new Account(accountNmbr, nextBalance, currency, status, userId, dateCreation, Instant.now());
    }

    public Account withdraw(double amount) {
        if (amount <= 0) {
            log.error("Attempted to withdraw a non-positive amount: {} from account {}", amount, accountNmbr);
            throw new IllegalArgumentException("Withdraw amount must be positive");
        }

        BigDecimal requestedAmount = BigDecimal.valueOf(amount);
        if (requestedAmount.compareTo(currentBalance) > 0) {
            log.error("Attempted to withdraw {} from account {} with insufficient balance. Current balance: {}",
                    amount, accountNmbr, currentBalance);
            throw new InsufficientBalanceException("Insufficient balance");
        }

        BigDecimal nextBalance = currentBalance.subtract(requestedAmount);
        return new Account(accountNmbr, nextBalance, currency, status, userId, dateCreation, Instant.now());
    }
}
