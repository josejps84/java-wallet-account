package com.wallet.account.application.services;

import org.springframework.stereotype.Service;

import com.wallet.account.domain.model.Account;
import com.wallet.account.domain.ports.in.CreateAccountUseCase;
import com.wallet.account.domain.ports.in.DepositUseCase;
import com.wallet.account.domain.ports.in.WithdrawUseCase;
import com.wallet.account.domain.ports.out.AccountRepositoryPort;
import com.wallet.account.domain.ports.in.GetBalanceUseCase;


@Service
public class AccountService implements CreateAccountUseCase,
        DepositUseCase,
        WithdrawUseCase,
        GetBalanceUseCase {

    private final AccountRepositoryPort accountRepository;

    public AccountService(AccountRepositoryPort accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    public Account createAccount(Account account) {
        return accountRepository.save(account);
    }

    @Override
    public void deposit(String accountId, double amount) {
        Account account = accountRepository.findById(accountId);
        if (account != null) {
            Account modifiedAccount = account.deposit(amount);
            accountRepository.save(modifiedAccount);
        }
        else {
            System.out.println("Account not found for ID: " + accountId);
            throw new IllegalArgumentException("Account not found");
        }
    }

    @Override
    public void withdraw(String accountId, double amount) {
        Account account = accountRepository.findById(accountId);
        if (account != null) {
            Account modifiedAccount = account.withdraw(amount);
            accountRepository.save(modifiedAccount);
        }
        else {
            System.out.println("Account not found for ID: " + accountId);
            throw new IllegalArgumentException("Account not found");
        }
    }

    @Override
    public double getBalance(String accountId) {
        Account account = accountRepository.findById(accountId);
        return (account != null) ? account.balance() : 0.0;
    }
    
}
