package com.wallet.account.infrastructure.adapters.out.db;

import java.math.BigDecimal;

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
        AccountEntity entity = new AccountEntity(account.email(), new BigDecimal(account.balance()), account.createdAt(), account.lastUpdatedAt());
        AccountEntity saved = repository.save(entity);
        return new Account(saved.getEmail(), saved.getBalance(), saved.getCreatedAt(), saved.getLastUpdatedAt());
    }

    @Override
    public Account findById(String accountId) {
        // Implement the logic to find the account by ID from the PostgreSQL database
        System.out.println("Finding account by ID: " + accountId);
        return repository.findById(accountId)
                .map(entity -> new Account(entity.getEmail(), entity.getBalance(), entity.getCreatedAt(), entity.getLastUpdatedAt()))
                .orElse(null);
    }
}
