package com.wallet.account.application.services;

import com.wallet.account.domain.model.Account;
import com.wallet.account.domain.model.AccountEvent;
import com.wallet.account.domain.model.AccountEventType;
import com.wallet.account.domain.model.AccountStatus;
import com.wallet.account.domain.model.Caller;
import com.wallet.account.domain.model.PagedResult;
import com.wallet.account.domain.model.exception.AccountAccessDeniedException;
import com.wallet.account.domain.model.exception.AccountNotFoundException;
import com.wallet.account.domain.ports.in.CreateAccountUseCase;
import com.wallet.account.domain.ports.in.DepositUseCase;
import com.wallet.account.domain.ports.in.EditAccountUseCase;
import com.wallet.account.domain.ports.in.GetAccountsUseCase;
import com.wallet.account.domain.ports.in.GetBalanceUseCase;
import com.wallet.account.domain.ports.in.WithdrawUseCase;
import com.wallet.account.domain.ports.out.AccountEventPublisherPort;
import com.wallet.account.domain.ports.out.AccountRepositoryPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
@Service
public class AccountService implements CreateAccountUseCase,
        DepositUseCase,
        WithdrawUseCase,
        GetBalanceUseCase,
        GetAccountsUseCase,
        EditAccountUseCase {

    private final AccountRepositoryPort accountRepository;
    private final AccountEventPublisherPort eventPublisher;

    public AccountService(AccountRepositoryPort accountRepository, AccountEventPublisherPort eventPublisher) {
        this.accountRepository = accountRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public Account createAccount(Caller caller, Long targetUserId, String currency) {
        long ownerId = caller.userId();
        if (targetUserId != null && targetUserId != caller.userId()) {
            if (!caller.isAdmin()) {
                throw new AccountAccessDeniedException("No puedes crear cuentas para otro usuario");
            }
            ownerId = targetUserId;
        }

        // Account numbers are generated server side so a client can never overwrite an existing account.
        String accountNmbr = UUID.randomUUID().toString();

        Account created = accountRepository.save(Account.open(accountNmbr, ownerId, currency));
        log.info("Account {} created for user {} by user {}", accountNmbr, ownerId, caller.userId());
        eventPublisher.publish(AccountEvent.of(AccountEventType.ACCOUNT_CREATED, created, caller, null));
        return created;
    }

    @Override
    @Transactional
    public Account editAccount(Caller caller, String accountNmbr, String currency, AccountStatus status) {
        if (!caller.isAdmin()) {
            throw new AccountAccessDeniedException("Solo un administrador puede editar cuentas");
        }
        Account account = loadForUpdate(accountNmbr);
        Account updated = accountRepository.save(account.updateData(currency, status));
        eventPublisher.publish(AccountEvent.of(AccountEventType.ACCOUNT_UPDATED, updated, caller, null));
        return updated;
    }

    @Override
    @Transactional
    public Account deposit(Caller caller, String accountNmbr, BigDecimal amount) {
        Account account = loadForUpdate(accountNmbr);
        checkAccess(caller, account);
        Account updated = accountRepository.save(account.deposit(amount));
        eventPublisher.publish(AccountEvent.of(AccountEventType.DEPOSIT, updated, caller, amount));
        return updated;
    }

    @Override
    @Transactional
    public Account withdraw(Caller caller, String accountNmbr, BigDecimal amount) {
        Account account = loadForUpdate(accountNmbr);
        checkAccess(caller, account);
        Account updated = accountRepository.save(account.withdraw(amount));
        eventPublisher.publish(AccountEvent.of(AccountEventType.WITHDRAWAL, updated, caller, amount));
        return updated;
    }

    @Override
    @Transactional(readOnly = true)
    public Account getAccount(Caller caller, String accountNmbr) {
        Account account = accountRepository.findById(accountNmbr)
                .orElseThrow(() -> notFound(accountNmbr));
        checkAccess(caller, account);
        return account;
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResult<Account> getAccounts(Caller caller, int page, int pageSize) {
        return caller.isAdmin()
                ? accountRepository.findAllAccounts(page, pageSize)
                : accountRepository.findAllAccountsByUserId(page, pageSize, caller.userId());
    }

    private Account loadForUpdate(String accountNmbr) {
        return accountRepository.findByIdForUpdate(accountNmbr)
                .orElseThrow(() -> notFound(accountNmbr));
    }

    private static void checkAccess(Caller caller, Account account) {
        if (!caller.canAccess(account)) {
            log.warn("User {} tried to access account {} owned by {}", caller.userId(), account.accountNmbr(),
                    account.userId());
            // Same answer as a missing account so foreign account numbers can't be probed.
            throw notFound(account.accountNmbr());
        }
    }

    private static AccountNotFoundException notFound(String accountNmbr) {
        log.warn("Account not found: {}", accountNmbr);
        return new AccountNotFoundException("Cuenta no encontrada");
    }
}
