package com.wallet.account.infrastructure.adapters.out.db;

import com.wallet.account.domain.model.Account;

final class AccountEntityMapper {

    private AccountEntityMapper() {
    }

    static AccountEntity toEntity(Account account) {
        return AccountEntity.builder()
                .accountNmbr(account.accountNmbr())
                .currentBalance(account.currentBalance())
                .currency(account.currency())
                .status(account.status())
                .userId(account.userId())
                .dateCreation(account.dateCreation())
                .lastUpdate(account.lastUpdate())
                .build();
    }

    static Account toDomain(AccountEntity entity) {
        return Account.builder()
                .accountNmbr(entity.getAccountNmbr())
                .currentBalance(entity.getCurrentBalance())
                .currency(entity.getCurrency())
                .status(entity.getStatus())
                .userId(entity.getUserId())
                .dateCreation(entity.getDateCreation())
                .lastUpdate(entity.getLastUpdate())
                .build();
    }
}
