package com.wallet.account.domain.ports.out;

import com.wallet.account.domain.model.Account;

import java.util.List;

public interface AccountRepositoryPort {
    Account save(Account account);
    Account findById(String accountId);
    List<Account> findAllAccounts(int page);
    List<Account> findAllAccounts(int page, long userId);
}
