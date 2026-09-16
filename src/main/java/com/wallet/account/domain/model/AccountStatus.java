package com.wallet.account.domain.model;

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
}
