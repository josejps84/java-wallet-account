package com.wallet.account.domain.ports.out;

import com.wallet.account.domain.model.Account;

public interface AccountRepositoryPort {
    Account save(Account account);
    Account findById(String accountId);
}
