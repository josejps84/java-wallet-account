package com.wallet.account.infrastructure.adapters.out.db;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SpringDataPostgresAccountRepository extends JpaRepository<AccountEntity, String> {
    Optional<AccountEntity> findById(String accountId);
}