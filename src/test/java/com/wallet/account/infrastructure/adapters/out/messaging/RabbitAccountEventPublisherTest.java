package com.wallet.account.infrastructure.adapters.out.messaging;

import com.wallet.account.domain.model.AccountEvent;
import com.wallet.account.domain.model.AccountEventType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.AmqpConnectException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.net.ConnectException;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class RabbitAccountEventPublisherTest {

    private final RabbitTemplate rabbitTemplate = mock(RabbitTemplate.class);
    private final RabbitAccountEventPublisher publisher =
            new RabbitAccountEventPublisher(rabbitTemplate, "audit_exchange", "audit_key");
    private final AccountEvent event = new AccountEvent("evt-1", AccountEventType.DEPOSIT, "acc-1", 1L, 1L,
            BigDecimal.TEN, BigDecimal.TEN, Instant.now());

    @AfterEach
    void clearSynchronization() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    @Test
    void withoutTransactionTheEventIsSentRightAway() {
        publisher.publish(event);

        verify(rabbitTemplate).convertAndSend("audit_exchange", "audit_key", event);
    }

    @Test
    void insideATransactionTheEventWaitsForTheCommit() {
        TransactionSynchronizationManager.initSynchronization();

        publisher.publish(event);
        verify(rabbitTemplate, never()).convertAndSend(anyString(), anyString(), any(Object.class));

        TransactionSynchronizationManager.getSynchronizations().forEach(TransactionSynchronization::afterCommit);
        verify(rabbitTemplate).convertAndSend("audit_exchange", "audit_key", event);
    }

    @Test
    void aRolledBackTransactionSendsNothing() {
        TransactionSynchronizationManager.initSynchronization();

        publisher.publish(event);
        TransactionSynchronizationManager.getSynchronizations()
                .forEach(sync -> sync.afterCompletion(TransactionSynchronization.STATUS_ROLLED_BACK));

        verify(rabbitTemplate, never()).convertAndSend(anyString(), anyString(), any(Object.class));
    }

    @Test
    void brokerFailuresDoNotBreakTheOperation() {
        doThrow(new AmqpConnectException(new ConnectException("down")))
                .when(rabbitTemplate).convertAndSend(anyString(), anyString(), any(Object.class));

        assertThatCode(() -> publisher.publish(event)).doesNotThrowAnyException();
    }
}
