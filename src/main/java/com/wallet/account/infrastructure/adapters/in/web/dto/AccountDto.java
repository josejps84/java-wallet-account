package com.wallet.account.infrastructure.adapters.in.web.dto;

import com.wallet.account.domain.model.Account;
import com.wallet.account.domain.model.AccountStatus;

import java.math.BigDecimal;
import java.time.Instant;

public record AccountDto(
        String accountNmbr,
        BigDecimal currentBalance,
        String currency,
        AccountStatus status,
        Long userId,
        Instant dateCreation,
        Instant lastUpdate
) {

    public static AccountDto from(Account account) {
        return new AccountDto(account.accountNmbr(), account.currentBalance(), account.currency(),
                account.status(), account.userId(), account.dateCreation(), account.lastUpdate());
    }
}
