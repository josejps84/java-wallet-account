package com.wallet.account.infrastructure.adapters.out.db;

import com.wallet.account.domain.model.AccountStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/** Persists {@link AccountStatus} by its explicit {@code value}, not by ordinal. */
@Converter
public class AccountStatusConverter implements AttributeConverter<AccountStatus, Integer> {

    @Override
    public Integer convertToDatabaseColumn(AccountStatus status) {
        return status == null ? null : status.getValue();
    }

    @Override
    public AccountStatus convertToEntityAttribute(Integer value) {
        return value == null ? null : AccountStatus.fromValue(value);
    }
}
