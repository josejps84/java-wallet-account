package com.wallet.account.domain.ports.in;

public interface GetBalanceUseCase {
    double getBalance(String accountId);
}
