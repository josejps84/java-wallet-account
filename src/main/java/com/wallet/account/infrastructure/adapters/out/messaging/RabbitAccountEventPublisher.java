package com.wallet.account.infrastructure.adapters.out.messaging;

import com.wallet.account.domain.model.AccountEvent;
import com.wallet.account.domain.ports.out.AccountEventPublisherPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Sends audit events to RabbitMQ only after the surrounding transaction commits, so the audit log never records
 * operations that were rolled back. A broker failure is logged and never undoes the business operation.
 */
@Slf4j
@Component
public class RabbitAccountEventPublisher implements AccountEventPublisherPort {

    private final RabbitTemplate rabbitTemplate;
    private final String exchangeName;
    private final String routingKey;

    public RabbitAccountEventPublisher(RabbitTemplate rabbitTemplate,
                                       @Value("${app.audit.exchange}") String exchangeName,
                                       @Value("${app.audit.routing-key}") String routingKey) {
        this.rabbitTemplate = rabbitTemplate;
        this.exchangeName = exchangeName;
        this.routingKey = routingKey;
    }

    @Override
    public void publish(AccountEvent event) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    send(event);
                }
            });
        } else {
            send(event);
        }
    }

    private void send(AccountEvent event) {
        try {
            rabbitTemplate.convertAndSend(exchangeName, routingKey, event);
            log.debug("Audit event {} sent ({})", event.eventId(), event.type());
        } catch (AmqpException ex) {
            log.error("Could not publish audit event {} ({}) for account {}", event.eventId(), event.type(),
                    event.accountNmbr(), ex);
        }
    }
}
