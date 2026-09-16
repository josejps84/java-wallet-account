package com.wallet.account.domain.ports.in;

import com.wallet.account.domain.model.Account;

public interface EditAccountUseCase {
    Account editAccount(Account accountData);
}
