package com.wallet.account.domain.ports.in;

public interface WithdrawUseCase {
    void withdraw(String accountId, double amount);
}
