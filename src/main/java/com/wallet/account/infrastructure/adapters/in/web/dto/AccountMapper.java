package com.wallet.account.infrastructure.adapters.in.web.dto;

import java.time.Instant;
import java.math.BigDecimal;
import com.wallet.account.domain.model.AccountStatus;

import com.wallet.account.domain.model.Account;

public class AccountMapper {

    public static AccountDto toDto(Account account) {
        return AccountDto.builder()
                .accountNmbr(account.accountNmbr())
                .currentBalance(account.currentBalance())
                .currency(account.currency())
                .status(account.status())
                .userId(account.userId())
                .dateCreation(account.dateCreation())
                .lastUpdate(account.lastUpdate())
                    .build();
    }

    public static Account toDomain(AccountCreationDto accountCreationRequest) {
        Instant now = Instant.now();
        return Account.builder()
                .accountNmbr(accountCreationRequest.getAccountNmbr())
                .currentBalance(BigDecimal.ZERO)
                .currency("USD")
                .status(AccountStatus.ACTIVE)
                .userId(accountCreationRequest.getUserId())
                .dateCreation(now)
                .lastUpdate(now)
                .build();
    }

    public static Account toDomain(AccountEditDto accountEditRequest) {
        return Account.builder()
                .accountNmbr(accountEditRequest.getAccountNmbr())
                .currency(accountEditRequest.getCurrency())
                .status(accountEditRequest.getStatus())
                .build();
    }
}
