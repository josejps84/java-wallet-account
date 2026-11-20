package com.wallet.account.domain.ports.in;

import com.wallet.account.domain.model.Account;
import com.wallet.account.domain.model.Caller;

import java.math.BigDecimal;

public interface DepositUseCase {
    Account deposit(Caller caller, String accountNmbr, BigDecimal amount);
}
