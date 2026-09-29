package com.wallet.account.infrastructure.config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${spring.rabbitmq.exchange}")
    private String exchangeName;

    @Value("${spring.rabbitmq.queue}")
    private String queueName;

    @Value("${spring.rabbitmq.routing-key}")
    private String routingKey;

    // 1. Definir la cola
    @Bean
    public Queue queue() {
        return new Queue(queueName, true); // true para que sea durable
    }

    // 2. Definir el Exchange (en este caso Direct)
    @Bean
    public DirectExchange exchange() {
        return new DirectExchange(exchangeName);
    }

    // 3. Unir la cola al exchange usando la Routing Key
    @Bean
    public Binding binding(Queue queue, DirectExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(routingKey);
    }
}
