package com.wallet.account.infrastructure.adapters.out.db;

import com.wallet.account.domain.model.Account;
import com.wallet.account.domain.model.AccountStatus;

public class AccountEntityMapper {
    
    public static AccountEntity toEntity(Account account) {
        return AccountEntity.builder()
                .accountNmbr(account.accountNmbr())
                .currentBalance(account.currentBalance())
                .currency(account.currency())
                .status(account.status().ordinal())
                .userId(account.userId())
                .dateCreation(account.dateCreation())
                .lastUpdate(account.lastUpdate())
                .build();
    }

    public static Account toDomain(AccountEntity entity) {
        return Account.builder()
                .accountNmbr(entity.getAccountNmbr())
                .currentBalance(entity.getCurrentBalance())
                .currency(entity.getCurrency())
                .status(AccountStatus.values()[entity.getStatus()])
                .userId(entity.getUserId())
                .dateCreation(entity.getDateCreation())
                .lastUpdate(entity.getLastUpdate())
                .build();
    }
}
