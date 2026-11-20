package com.wallet.account.application.services;

import com.wallet.account.domain.model.Account;
import com.wallet.account.domain.model.AccountEvent;
import com.wallet.account.domain.model.AccountEventType;
import com.wallet.account.domain.model.AccountStatus;
import com.wallet.account.domain.model.Caller;
import com.wallet.account.domain.model.UserType;
import com.wallet.account.domain.model.exception.AccountAccessDeniedException;
import com.wallet.account.domain.model.exception.AccountNotFoundException;
import com.wallet.account.domain.ports.out.AccountEventPublisherPort;
import com.wallet.account.domain.ports.out.AccountRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    private static final Caller OWNER = new Caller(1L, UserType.CUSTOMER);
    private static final Caller OTHER = new Caller(2L, UserType.CUSTOMER);
    private static final Caller ADMIN = new Caller(99L, UserType.ADMIN);

    @Mock
    AccountRepositoryPort repository;
    @Mock
    AccountEventPublisherPort publisher;
    @InjectMocks
    AccountService service;

    private final Account account = Account.open("acc-1", 1L, "USD").deposit(new BigDecimal("50"));

    @Test
    void customerCreatesAccountForThemselvesWithGeneratedNumber() {
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Account created = service.createAccount(OWNER, null, null);

        assertThat(created.userId()).isEqualTo(1L);
        assertThat(created.accountNmbr()).isNotBlank();
        verify(publisher).publish(any());
    }

    @Test
    void customerCannotCreateAccountForAnotherUser() {
        assertThatThrownBy(() -> service.createAccount(OWNER, 2L, null))
                .isInstanceOf(AccountAccessDeniedException.class);
        verify(repository, never()).save(any());
    }

    @Test
    void adminCanCreateAccountForAnotherUser() {
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        assertThat(service.createAccount(ADMIN, 5L, "EUR").userId()).isEqualTo(5L);
    }

    @Test
    void ownerCanWithdrawAndAnEventIsPublished() {
        when(repository.findByIdForUpdate("acc-1")).thenReturn(Optional.of(account));
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Account updated = service.withdraw(OWNER, "acc-1", new BigDecimal("20"));

        assertThat(updated.currentBalance()).isEqualByComparingTo("30");
        ArgumentCaptor<AccountEvent> event = ArgumentCaptor.forClass(AccountEvent.class);
        verify(publisher).publish(event.capture());
        assertThat(event.getValue().type()).isEqualTo(AccountEventType.WITHDRAWAL);
        assertThat(event.getValue().amount()).isEqualByComparingTo("20");
    }

    @Test
    void otherCustomerCannotTouchTheAccount() {
        when(repository.findByIdForUpdate("acc-1")).thenReturn(Optional.of(account));

        assertThatThrownBy(() -> service.withdraw(OTHER, "acc-1", BigDecimal.ONE))
                .isInstanceOf(AccountNotFoundException.class);
        verify(repository, never()).save(any());
        verify(publisher, never()).publish(any());
    }

    @Test
    void balanceOfMissingAccountIsNotFound() {
        when(repository.findById("nope")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getAccount(ADMIN, "nope")).isInstanceOf(AccountNotFoundException.class);
    }

    @Test
    void onlyAdminsCanEditAccounts() {
        assertThatThrownBy(() -> service.editAccount(OWNER, "acc-1", "EUR", AccountStatus.INACTIVE))
                .isInstanceOf(AccountAccessDeniedException.class);

        when(repository.findByIdForUpdate("acc-1")).thenReturn(Optional.of(account));
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        Account edited = service.editAccount(ADMIN, "acc-1", "EUR", AccountStatus.INACTIVE);
        assertThat(edited.currency()).isEqualTo("EUR");
        assertThat(edited.status()).isEqualTo(AccountStatus.INACTIVE);
    }

    @Test
    void customersOnlyListTheirOwnAccounts() {
        service.getAccounts(OWNER, 0, 10);
        verify(repository).findAllAccountsByUserId(0, 10, 1L);

        service.getAccounts(ADMIN, 0, 10);
        verify(repository).findAllAccounts(0, 10);
    }
}
