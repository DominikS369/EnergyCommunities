package usageservice;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import shared.EnergyMessage;
import shared.UpdateMessage;

import java.time.LocalDateTime;

@Component
public class UsageListener {

    private final EnergyUsageRepository repository;
    private final RabbitTemplate rabbitTemplate;

    public UsageListener(EnergyUsageRepository repository, RabbitTemplate rabbitTemplate) {
        this.repository = repository;
        this.rabbitTemplate = rabbitTemplate;
    }

    @RabbitListener(queues = RabbitConfig.INPUT_QUEUE)
    @Transactional
    public void handle(EnergyMessage msg) {
        LocalDateTime hour = msg.getDatetime().withMinute(0).withSecond(0).withNano(0);

        EnergyUsage usage = repository.findByHour(hour)
                .orElseGet(() -> new EnergyUsage(hour));

        switch (msg.getType()) {
            case "PRODUCER" -> handleProducer(usage, msg.getKwh());
            case "USER"     -> handleUser(usage, msg.getKwh());
            default         -> {
                System.err.println("Unknown message type: " + msg.getType());
                return;
            }
        }

        repository.save(usage);

        rabbitTemplate.convertAndSend(
                RabbitConfig.EXCHANGE,
                RabbitConfig.UPDATE_KEY,
                new UpdateMessage(hour)
        );

        System.out.printf("Processed %s | hour=%s | produced=%.3f | used=%.3f | grid=%.3f%n",
                msg.getType(), hour, usage.getCommunityProduced(),
                usage.getCommunityUsed(), usage.getGridUsed());
    }

    private void handleProducer(EnergyUsage usage, double kwh) {
        usage.setCommunityProduced(round3(usage.getCommunityProduced() + kwh));
    }

    private void handleUser(EnergyUsage usage, double kwh) {
        double available = usage.getCommunityProduced() - usage.getCommunityUsed();
        double fromCommunity = Math.min(kwh, Math.max(available, 0.0));
        double fromGrid = kwh - fromCommunity;

        usage.setCommunityUsed(round3(usage.getCommunityUsed() + fromCommunity));
        usage.setGridUsed(round3(usage.getGridUsed() + fromGrid));
    }

    private double round3(double value) {
        return Math.round(value * 1000.0) / 1000.0;
    }
}
