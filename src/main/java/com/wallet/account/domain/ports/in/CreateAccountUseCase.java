package com.wallet.account.domain.ports.in;

import com.wallet.account.domain.model.Account;
import com.wallet.account.domain.model.Caller;

public interface CreateAccountUseCase {

    /**
     * Opens a new account. Customers can only open accounts for themselves; admins may pass
     * {@code targetUserId} to open one for another user.
     */
    Account createAccount(Caller caller, Long targetUserId, String currency);
}
