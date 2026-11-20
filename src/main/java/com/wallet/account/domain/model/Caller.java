package com.wallet.account.domain.model;

/**
 * The authenticated user performing an operation, as taken from the validated JWT.
 */
public record Caller(long userId, UserType userType) {

    public boolean isAdmin() {
        return userType == UserType.ADMIN;
    }

    public boolean canAccess(Account account) {
        return isAdmin() || account.isOwnedBy(userId);
    }
}
