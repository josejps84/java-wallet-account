package com.wallet.account.domain.ports.in;

import com.wallet.account.domain.model.Account;
import com.wallet.account.domain.model.AccountStatus;
import com.wallet.account.domain.model.Caller;

public interface EditAccountUseCase {
    Account editAccount(Caller caller, String accountNmbr, String currency, AccountStatus status);
}
