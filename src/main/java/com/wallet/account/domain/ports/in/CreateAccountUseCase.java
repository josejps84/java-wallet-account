package com.wallet.account.domain.ports.in;

import com.wallet.account.domain.model.Account;

public interface CreateAccountUseCase {
    Account createAccount(Account account);
}
