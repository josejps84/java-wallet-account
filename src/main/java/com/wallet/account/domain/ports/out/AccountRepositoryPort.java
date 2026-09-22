package com.wallet.account.domain.ports.out;

import com.wallet.account.domain.model.Account;
import com.wallet.account.domain.model.PagedResult;

public interface AccountRepositoryPort {
    Account save(Account account);
    Account findById(String accountId);
    PagedResult<Account> findAllAccounts(int page, int pageSize);
    PagedResult<Account> findAllAccounts(int page, int pageSize, long userId);
}
