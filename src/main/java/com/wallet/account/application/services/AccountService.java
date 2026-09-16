package com.wallet.account.application.services;

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
    private final AuthenticationPort authenticator;

    public AccountService(AccountRepositoryPort accountRepository, AuthenticationPort authenticator) {
        this.accountRepository = accountRepository;
        this.authenticator = authenticator;
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
    public List<AccountDto> getAllAccounts(int page, String token) {
        // Implement the logic to retrieve all accounts based on the page and token
        // For now, returning an empty list as a placeholder
        UserType userType = GetUserTypeFromToken(token);
        if (userType == UserType.ADMIN) {
            // Retrieve all accounts for admin users
            List<AccountDto> accountList = accountRepository.findAllAccounts(page).stream()
                    .map(AccountMapper::toDto)
                    .toList();
            return accountList;
        } else {
            // For non-admin users, return an empty list or handle accordingly
            log.warn("Non-admin user attempted to access all accounts");
        }
        return new ArrayList<>();
    }

    private UserType GetUserTypeFromToken(String token) {
        Result<Claims, AuthenticationResult> result = authenticator.validateToken(token);
        if (result instanceof Result.Success<Claims, AuthenticationResult> success) {
            Claims claims = success.value();
            String userTypeStr = claims.get("userType", String.class);
            return UserType.valueOf(userTypeStr);
        } else {
            log.error("Invalid token: {}", result);
            throw new RuntimeException("Invalid token");
        }
    }

}
