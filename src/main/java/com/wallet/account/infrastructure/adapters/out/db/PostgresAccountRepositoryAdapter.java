package com.wallet.account.infrastructure.adapters.out.db;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import com.wallet.account.domain.model.Account;
import com.wallet.account.domain.ports.out.AccountRepositoryPort;

import java.util.List;
import java.util.stream.Collectors;

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

    @Override
    public List<Account> findAllAccounts(int page) {
        // Implement the logic to retrieve all accounts from the PostgreSQL database
        int pageSize = 10; // Define the number of accounts per page
        Pageable pageable = PageRequest.of(page, pageSize);
        return repository.findAll(pageable)
                .stream()
                .map(AccountEntityMapper::toDomain)
                .collect(Collectors.toList());
    }
}
