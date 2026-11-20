package com.wallet.account.domain.ports.in;

import com.wallet.account.domain.model.Account;
import com.wallet.account.domain.model.Caller;
import com.wallet.account.domain.model.PagedResult;

public interface GetAccountsUseCase {

    /** Admins get every account; customers only their own. */
    PagedResult<Account> getAccounts(Caller caller, int page, int pageSize);
}
