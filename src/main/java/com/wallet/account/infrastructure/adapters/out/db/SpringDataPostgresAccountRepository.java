package com.wallet.account.infrastructure.adapters.out.db;

import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface SpringDataPostgresAccountRepository extends JpaRepository<AccountEntity, String> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from AccountEntity a where a.accountNmbr = :accountNmbr")
    Optional<AccountEntity> findByIdForUpdate(@Param("accountNmbr") String accountNmbr);

    Page<AccountEntity> findByUserId(long userId, Pageable pageable);
}
