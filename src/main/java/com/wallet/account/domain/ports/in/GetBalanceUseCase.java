package com.wallet.account.domain.ports.in;

import com.wallet.account.domain.model.Account;
import com.wallet.account.domain.model.Caller;

public interface GetBalanceUseCase {
    Account getAccount(Caller caller, String accountNmbr);
}
