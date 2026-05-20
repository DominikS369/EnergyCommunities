package usageservice;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    // Shared with producer
    public static final String EXCHANGE       = "energy.exchange";
    public static final String INPUT_QUEUE    = "energy.messages";
    public static final String INPUT_KEY      = "energy.input";

    // Published after each DB update → consumed by Current Percentage Service
    public static final String UPDATE_QUEUE   = "energy.updates";
    public static final String UPDATE_KEY     = "energy.update";

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
        return BindingBuilder.bind(inputQueue).to(energyExchange).with(INPUT_KEY);
    }

    @Bean
    public Queue updateQueue() {
        return new Queue(UPDATE_QUEUE, true);
    }

    @Bean
    public Binding updateBinding(Queue updateQueue, DirectExchange energyExchange) {
        return BindingBuilder.bind(updateQueue).to(energyExchange).with(UPDATE_KEY);
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
