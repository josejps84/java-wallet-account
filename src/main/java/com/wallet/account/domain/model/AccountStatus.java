package com.wallet.account.domain.model;

public enum AccountStatus {
    ACTIVE(1),
    INACTIVE(0);

    private final int value;

    AccountStatus(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
