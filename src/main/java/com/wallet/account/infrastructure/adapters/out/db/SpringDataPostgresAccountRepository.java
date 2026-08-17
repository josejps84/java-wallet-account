package com.wallet.account.infrastructure.adapters.out.db;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SpringDataPostgresAccountRepository extends JpaRepository<AccountEntity, String> {
    Optional<AccountEntity> findById(String accountId);
}