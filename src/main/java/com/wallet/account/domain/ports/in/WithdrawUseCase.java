package com.wallet.account.domain.ports.in;

import com.wallet.account.domain.model.Account;
import com.wallet.account.domain.model.Caller;

import java.math.BigDecimal;

public interface WithdrawUseCase {
    Account withdraw(Caller caller, String accountNmbr, BigDecimal amount);
}
