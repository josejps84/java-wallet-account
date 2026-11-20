package com.wallet.account.domain.model;

import java.util.Arrays;

public enum AccountStatus {
    INACTIVE(0),
    ACTIVE(1);

    private final int value;

    AccountStatus(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static AccountStatus fromValue(int value) {
        return Arrays.stream(values())
                .filter(status -> status.value == value)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown account status value: " + value));
    }
}
