package producer;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import shared.EnergyMessage;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Random;

@Component
public class ProducerScheduler {

    // Max kWh pro Minute einer kleinen Solaranlage (~5 kWp)
    private static final double MAX_KWH_PER_MINUTE = 0.083;

    private final RabbitTemplate rabbitTemplate;
    private final WeatherService weatherService;
    private final Random random = new Random();

    public ProducerScheduler(RabbitTemplate rabbitTemplate, WeatherService weatherService) {
        this.rabbitTemplate = rabbitTemplate;
        this.weatherService = weatherService;
    }

    @Scheduled(fixedDelay = 5000)
    public void sendProductionMessage() {
        double radiation = weatherService.getDirectRadiation();

        // Skalierung: 1000 W/m² = maximale Sonneneinstrahlung
        double radiationFactor = Math.min(radiation / 1000.0, 1.0);

        // ±15 % Zufallsanteil
        double noise = 1.0 + (random.nextDouble() * 0.3 - 0.15);
        double kwh = Math.round(MAX_KWH_PER_MINUTE * radiationFactor * noise * 1000.0) / 1000.0;
        kwh = Math.max(kwh, 0.0001);

        EnergyMessage msg = new EnergyMessage(
                "PRODUCER",
                "COMMUNITY",
                kwh,
                LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES)
        );

        rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE, RabbitConfig.ROUTING_KEY, msg);
        System.out.printf("Sent PRODUCER | radiation=%.1f W/m² | kwh=%.4f%n", radiation, kwh);
    }
}