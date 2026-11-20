package com.wallet.account.domain.ports.out;

import com.wallet.account.domain.model.Account;
import com.wallet.account.domain.model.PagedResult;

import java.util.Optional;

public interface AccountRepositoryPort {
    Account save(Account account);
    Optional<Account> findById(String accountNmbr);

    /** Loads the account locking its row until the current transaction ends. */
    Optional<Account> findByIdForUpdate(String accountNmbr);

    PagedResult<Account> findAllAccounts(int page, int pageSize);
    PagedResult<Account> findAllAccountsByUserId(int page, int pageSize, long userId);
}
