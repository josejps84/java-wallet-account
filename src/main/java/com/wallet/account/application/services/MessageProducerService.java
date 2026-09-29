package com.wallet.account.application.services;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j 
public class MessageProducerService {


    @Value("${spring.rabbitmq.exchange}")
    private String exchangeName;

    @Value("${spring.rabbitmq.routing-key}")
    private String routingKey;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void enviarMensaje(String mensaje) {
        log.info("Enviando mensaje a RabbitMQ: " + mensaje);
        String mensajeJson = """
            {
                "contenido": "%s"
            }
            """.formatted(mensaje);
        
        // Enviamos al exchange con la routing key específica
        rabbitTemplate.convertAndSend(exchangeName, routingKey, mensajeJson);
    }
}
