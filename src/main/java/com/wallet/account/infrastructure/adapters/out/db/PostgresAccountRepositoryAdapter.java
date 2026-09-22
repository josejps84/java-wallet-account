package com.wallet.account.infrastructure.adapters.out.db;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import com.wallet.account.domain.model.Account;
import com.wallet.account.domain.model.PagedResult;
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
    public PagedResult<Account> findAllAccounts(int page, int pageSize) {
        // Implement the logic to retrieve all accounts from the PostgreSQL database
        Pageable pageable = PageRequest.of(page, pageSize);
        Page<AccountEntity> result = repository.findAll(pageable);
        List<Account> content = result.getContent().stream()
                .map(AccountEntityMapper::toDomain)
                .collect(Collectors.toList());
        return new PagedResult<>(content, result.getTotalElements());
    }

    @Override
    public PagedResult<Account> findAllAccounts(int page, int pageSize, long userId) {
        // Implement the logic to retrieve all accounts from the PostgreSQL database
        Pageable pageable = PageRequest.of(page, pageSize);
        List<Account> content = repository.findByUserId(userId, pageable).stream()
                .map(AccountEntityMapper::toDomain)
                .collect(Collectors.toList());
        long totalElements = repository.countByUserId(userId);
        return new PagedResult<>(content, totalElements);
    }
}
