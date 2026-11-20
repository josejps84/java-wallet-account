package com.wallet.account.domain.model;

import com.wallet.account.domain.model.exception.InsufficientBalanceException;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.Instant;

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

    public static final String DEFAULT_CURRENCY = "USD";

    public static Account open(String accountNmbr, long userId, String currency) {
        Instant now = Instant.now();
        return new Account(accountNmbr, BigDecimal.ZERO, currency != null ? currency : DEFAULT_CURRENCY,
                AccountStatus.ACTIVE, userId, now, now);
    }

    public boolean isOwnedBy(long candidateUserId) {
        return userId != null && userId == candidateUserId;
    }

    public Account deposit(BigDecimal amount) {
        requirePositive(amount, "El monto a depositar debe ser positivo");
        return withBalance(currentBalance.add(amount));
    }

    public Account withdraw(BigDecimal amount) {
        requirePositive(amount, "El monto a retirar debe ser positivo");
        if (amount.compareTo(currentBalance) > 0) {
            throw new InsufficientBalanceException("Saldo insuficiente");
        }
        return withBalance(currentBalance.subtract(amount));
    }

    public Account updateData(String newCurrency, AccountStatus newStatus) {
        return new Account(
                accountNmbr,
                currentBalance,
                newCurrency != null ? newCurrency : currency,
                newStatus != null ? newStatus : status,
                userId,
                dateCreation,
                Instant.now()
        );
    }

    private Account withBalance(BigDecimal nextBalance) {
        return new Account(accountNmbr, nextBalance, currency, status, userId, dateCreation, Instant.now());
    }

    private static void requirePositive(BigDecimal amount, String message) {
        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException(message);
        }
    }
}
