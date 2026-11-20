package com.wallet.account.domain.model;

import com.wallet.account.domain.model.exception.InsufficientBalanceException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AccountTest {

    private final Account account = Account.open("acc-1", 7L, null);

    @Test
    void openCreatesActiveUsdAccountWithZeroBalance() {
        assertThat(account.currentBalance()).isEqualByComparingTo("0");
        assertThat(account.currency()).isEqualTo("USD");
        assertThat(account.status()).isEqualTo(AccountStatus.ACTIVE);
        assertThat(account.isOwnedBy(7L)).isTrue();
        assertThat(account.isOwnedBy(8L)).isFalse();
    }

    @Test
    void depositAndWithdrawKeepExactDecimalPrecision() {
        Account updated = account.deposit(new BigDecimal("0.1")).deposit(new BigDecimal("0.2"));
        assertThat(updated.currentBalance()).isEqualByComparingTo("0.3");

        assertThat(updated.withdraw(new BigDecimal("0.3")).currentBalance()).isEqualByComparingTo("0");
    }

    @Test
    void withdrawMoreThanBalanceFails() {
        Account funded = account.deposit(BigDecimal.TEN);
        assertThatThrownBy(() -> funded.withdraw(new BigDecimal("10.01")))
                .isInstanceOf(InsufficientBalanceException.class);
    }

    @Test
    void nonPositiveOrMissingAmountsAreRejected() {
        assertThatThrownBy(() -> account.deposit(BigDecimal.ZERO)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> account.deposit(null)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> account.withdraw(new BigDecimal("-1"))).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void updateDataOnlyChangesProvidedFields() {
        Account updated = account.updateData(null, AccountStatus.INACTIVE);
        assertThat(updated.currency()).isEqualTo("USD");
        assertThat(updated.status()).isEqualTo(AccountStatus.INACTIVE);
        assertThat(updated.currentBalance()).isEqualTo(account.currentBalance());
    }

    @Test
    void statusIsResolvedByExplicitValue() {
        assertThat(AccountStatus.fromValue(0)).isEqualTo(AccountStatus.INACTIVE);
        assertThat(AccountStatus.fromValue(1)).isEqualTo(AccountStatus.ACTIVE);
        assertThatThrownBy(() -> AccountStatus.fromValue(5)).isInstanceOf(IllegalArgumentException.class);
    }
}
