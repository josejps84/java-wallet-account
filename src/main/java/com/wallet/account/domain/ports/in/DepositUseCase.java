package com.wallet.account.domain.ports.in;

public interface DepositUseCase {
    void deposit(String accountId, double amount);
}
