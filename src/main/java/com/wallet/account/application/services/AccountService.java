package com.wallet.account.application.services;

import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import com.wallet.account.domain.model.Account;
import com.wallet.account.domain.model.exception.AccountNotFoundException;
import com.wallet.account.domain.ports.in.CreateAccountUseCase;
import com.wallet.account.domain.ports.in.DepositUseCase;
import com.wallet.account.domain.ports.in.WithdrawUseCase;
import com.wallet.account.domain.ports.out.AccountRepositoryPort;
import com.wallet.account.domain.ports.in.GetBalanceUseCase;


@Slf4j
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
            log.error("Account not found for ID: {}", accountId);
            throw new AccountNotFoundException("Account not found");
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
            log.error("Account not found for ID: {}", accountId);
            throw new AccountNotFoundException("Account not found");
        }
    }

    @Override
    public double getBalance(String accountId) {
        Account account = accountRepository.findById(accountId);
        return (account != null) ? account.currentBalance().doubleValue() : 0.0;
    }
    
}
