package com.wallet.account.infrastructure.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

    @Bean
    public Queue auditQueue(@Value("${app.audit.queue}") String queueName) {
        return new Queue(queueName, true);
    }

    @Bean
    public DirectExchange auditExchange(@Value("${app.audit.exchange}") String exchangeName) {
        return new DirectExchange(exchangeName);
    }

    @Bean
    public Binding auditBinding(Queue auditQueue, DirectExchange auditExchange,
                                @Value("${app.audit.routing-key}") String routingKey) {
        return BindingBuilder.bind(auditQueue).to(auditExchange).with(routingKey);
    }

    /** Serializes events as JSON; picked up automatically by Boot's RabbitTemplate. */
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }
}
