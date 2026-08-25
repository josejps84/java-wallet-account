package com.wallet.account.infrastructure.adapters.out.db;

import org.springframework.stereotype.Component;

import com.wallet.account.domain.model.Account;
import com.wallet.account.domain.ports.out.AccountRepositoryPort;

@Component
public class PostgresAccountRepositoryAdapter implements AccountRepositoryPort {

    private final SpringDataPostgresAccountRepository repository;

    public PostgresAccountRepositoryAdapter(SpringDataPostgresAccountRepository repository) {
        this.repository = repository;
    }

    @Override
    public Account save(Account account) {
        // Implement the logic to save the account to the PostgreSQL database
        AccountEntity entity = AccountEntityMapper.toEntity(account);
        AccountEntity saved = repository.save(entity);
        return AccountEntityMapper.toDomain(saved);
    }

    @Override
    public Account findById(String accountId) {
        // Implement the logic to find the account by ID from the PostgreSQL database
        System.out.println("Finding account by ID: " + accountId);
        return repository.findById(accountId)
                .map(AccountEntityMapper::toDomain)
                .orElse(null);
    }
}
