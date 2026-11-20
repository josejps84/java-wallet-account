package com.wallet.account.domain.ports.out;

import com.wallet.account.domain.model.AccountEvent;

public interface AccountEventPublisherPort {

    /** Publishes the event once the current transaction (if any) commits. */
    void publish(AccountEvent event);
}
