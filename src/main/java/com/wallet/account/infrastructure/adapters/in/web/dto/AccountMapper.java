package com.wallet.account.infrastructure.adapters.in.web.dto;

import java.time.Instant;

import com.wallet.account.domain.model.Account;

public class AccountMapper {

    public static AccountDto toDto(Account account) {
        return AccountDto.builder()
                .email(account.email())
                .balance(account.balance())
                .build();
    }

    public static Account toDomain(AccountDto accountDto) {
        return Account.builder()
                .email(accountDto.getEmail())
                .balance(accountDto.getBalance() == null ? 0 : accountDto.getBalance())
                .createdAt(Instant.now())
                .lastUpdatedAt(Instant.now())
                .build();
    }
}
