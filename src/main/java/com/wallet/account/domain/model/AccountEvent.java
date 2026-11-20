package com.wallet.account.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Audit event emitted after an account changes.
 *
 * @param amount amount moved by the operation; {@code null} for non-monetary events
 */
public record AccountEvent(
        String eventId,
        AccountEventType type,
        String accountNmbr,
        Long accountUserId,
        long performedBy,
        BigDecimal amount,
        BigDecimal balance,
        Instant occurredAt
) {

    public static AccountEvent of(AccountEventType type, Account account, Caller caller, BigDecimal amount) {
        return new AccountEvent(UUID.randomUUID().toString(), type, account.accountNmbr(), account.userId(),
                caller.userId(), amount, account.currentBalance(), Instant.now());
    }
}
