package user;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import shared.EnergyMessage;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Random;

@Component
public class UserScheduler {

    // kWh pro Nachricht (Sendetakt ~7 s); Basis: typischer Haushalt ~3.5 kWh/Tag
    private static final double BASE_KWH_PER_MESSAGE = 0.0024;

    private final RabbitTemplate rabbitTemplate;
    private final Random random = new Random();

    public UserScheduler(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Scheduled(fixedDelay = 7000)
    public void sendUsageMessage() {
        // ±30% Zufallsanteil simuliert Tageszeit-Schwankungen
        double noise = 1.0 + (random.nextDouble() * 0.6 - 0.3);
        double kwh = Math.round(BASE_KWH_PER_MESSAGE * noise * 1000.0) / 1000.0;
        kwh = Math.max(kwh, 0.0001);

        EnergyMessage msg = new EnergyMessage(
                "USER",
                "COMMUNITY",
                kwh,
                LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES)
        );

        rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE, RabbitConfig.ROUTING_KEY, msg);
        System.out.printf("Sent USER | kwh=%.4f%n", kwh);
    }
}