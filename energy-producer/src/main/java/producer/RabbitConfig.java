package producer;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    public static final String EXCHANGE    = "energy.exchange";
    public static final String INPUT_QUEUE = "energy.messages";
    public static final String ROUTING_KEY = "energy.input";

    @Bean
    public DirectExchange energyExchange() {
        return new DirectExchange(EXCHANGE);
    }

    @Bean
    public Queue inputQueue() {
        return new Queue(INPUT_QUEUE, true);
    }

    @Bean
    public Binding inputBinding(Queue inputQueue, DirectExchange energyExchange) {
        return BindingBuilder.bind(inputQueue).to(energyExchange).with(ROUTING_KEY);
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}