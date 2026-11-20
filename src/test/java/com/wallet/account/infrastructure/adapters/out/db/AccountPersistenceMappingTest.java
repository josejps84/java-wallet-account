package com.wallet.account.infrastructure.adapters.out.db;

import com.wallet.account.domain.model.Account;
import com.wallet.account.domain.model.AccountStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AccountPersistenceMappingTest {

    private final AccountStatusConverter converter = new AccountStatusConverter();

    @Test
    void statusIsStoredByItsExplicitValue() {
        assertThat(converter.convertToDatabaseColumn(AccountStatus.INACTIVE)).isZero();
        assertThat(converter.convertToDatabaseColumn(AccountStatus.ACTIVE)).isEqualTo(1);
        assertThat(converter.convertToEntityAttribute(0)).isEqualTo(AccountStatus.INACTIVE);
        assertThat(converter.convertToEntityAttribute(1)).isEqualTo(AccountStatus.ACTIVE);
        assertThat(converter.convertToDatabaseColumn(null)).isNull();
        assertThat(converter.convertToEntityAttribute(null)).isNull();
        assertThatThrownBy(() -> converter.convertToEntityAttribute(9)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void entityMapperRoundTripKeepsEveryField() {
        Instant created = Instant.parse("2026-01-01T10:00:00Z");
        Account account = new Account("acc-1", new BigDecimal("12.3456"), "EUR", AccountStatus.INACTIVE, 9L,
                created, created.plusSeconds(30));

        assertThat(AccountEntityMapper.toDomain(AccountEntityMapper.toEntity(account))).isEqualTo(account);
    }
}
