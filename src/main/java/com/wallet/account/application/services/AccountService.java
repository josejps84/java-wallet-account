package com.wallet.account.application.services;

import com.wallet.account.domain.model.PagedResult;
import com.wallet.account.domain.model.UserType;
import com.wallet.account.domain.ports.in.*;
import com.wallet.account.infrastructure.adapters.in.web.dto.AccountDto;
import com.wallet.account.infrastructure.adapters.in.web.dto.AccountMapper;
import com.wallet.account.infrastructure.adapters.in.security.AuthenticationResult;
import com.wallet.account.domain.core.Result;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.ArrayList;
import lombok.extern.slf4j.Slf4j;
import com.wallet.account.domain.model.Account;
import com.wallet.account.domain.model.exception.AccountNotFoundException;
import com.wallet.account.domain.ports.out.AccountRepositoryPort;
import io.jsonwebtoken.Claims;


@Slf4j
@Service
public class AccountService implements CreateAccountUseCase,
        DepositUseCase,
        WithdrawUseCase,
        GetBalanceUseCase,
        GetAccountsUseCase,
        EditAccountUseCase {

    private final AccountRepositoryPort accountRepository;

    public AccountService(AccountRepositoryPort accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    public Account createAccount(Account account) {
        return accountRepository.save(account);
    }

    @Override
    public Account editAccount(Account accountData) {
        Account account = accountRepository.findById(accountData.accountNmbr());
        if (account != null) {
            Account modifiedAccount = account.updateData(accountData);
            accountRepository.save(modifiedAccount);
            return modifiedAccount;
        }
        else {
            log.error("Account not found for ID: {}", accountData.accountNmbr());
            throw new AccountNotFoundException("Account not found");
        }
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

    @Override
    public PagedResult<AccountDto> getAllAccounts(int page, int pageSize, UserType userType, long userId) {
        PagedResult<Account> accountsPage;
        if (userType == UserType.ADMIN) {
            // Retrieve all accounts for admin users
            accountsPage = accountRepository.findAllAccounts(page, pageSize);
        } else {
            accountsPage = accountRepository.findAllAccounts(page, pageSize, userId);
        }
        List<AccountDto> accountDtoList = accountsPage.content().stream()
                .map(AccountMapper::toDto)
                .toList();
        return new PagedResult<>(accountDtoList, accountsPage.totalElements());
    }

}
