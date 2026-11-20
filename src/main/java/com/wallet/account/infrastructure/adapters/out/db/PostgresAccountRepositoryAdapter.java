package com.wallet.account.infrastructure.adapters.out.db;

import com.wallet.account.domain.model.Account;
import com.wallet.account.domain.model.PagedResult;
import com.wallet.account.domain.ports.out.AccountRepositoryPort;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class PostgresAccountRepositoryAdapter implements AccountRepositoryPort {

    private static final Sort DEFAULT_SORT = Sort.by("dateCreation").descending().and(Sort.by("accountNmbr"));

    private final SpringDataPostgresAccountRepository repository;

    public PostgresAccountRepositoryAdapter(SpringDataPostgresAccountRepository repository) {
        this.repository = repository;
    }

    @Override
    public Account save(Account account) {
        return AccountEntityMapper.toDomain(repository.save(AccountEntityMapper.toEntity(account)));
    }

    @Override
    public Optional<Account> findById(String accountNmbr) {
        return repository.findById(accountNmbr).map(AccountEntityMapper::toDomain);
    }

    @Override
    public Optional<Account> findByIdForUpdate(String accountNmbr) {
        return repository.findByIdForUpdate(accountNmbr).map(AccountEntityMapper::toDomain);
    }

    @Override
    public PagedResult<Account> findAllAccounts(int page, int pageSize) {
        return toPagedResult(repository.findAll(pageRequest(page, pageSize)));
    }

    @Override
    public PagedResult<Account> findAllAccountsByUserId(int page, int pageSize, long userId) {
        return toPagedResult(repository.findByUserId(userId, pageRequest(page, pageSize)));
    }

    private static Pageable pageRequest(int page, int pageSize) {
        return PageRequest.of(page, pageSize, DEFAULT_SORT);
    }

    private static PagedResult<Account> toPagedResult(Page<AccountEntity> result) {
        return new PagedResult<>(result.getContent().stream().map(AccountEntityMapper::toDomain).toList(),
                result.getTotalElements());
    }
}
