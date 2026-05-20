package percentageservice;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import shared.UpdateMessage;

@Component
public class PercentageListener {

    private final EnergyUsageRepository usageRepository;
    private final CurrentPercentageRepository percentageRepository;

    public PercentageListener(EnergyUsageRepository usageRepository,
                              CurrentPercentageRepository percentageRepository) {
        this.usageRepository = usageRepository;
        this.percentageRepository = percentageRepository;
    }

    @RabbitListener(queues = RabbitConfig.UPDATE_QUEUE)
    @Transactional
    public void handle(UpdateMessage msg) {
        usageRepository.findByHour(msg.getHour()).ifPresent(usage -> {
            double produced = usage.getCommunityProduced();
            double used     = usage.getCommunityUsed();
            double grid     = usage.getGridUsed();
            double total    = used + grid;

            // Wieviel % der Produktion wurde verbraucht (0–100)
            double communityDepleted = produced > 0 ? round2(used / produced * 100.0) : 0.0;
            // Wieviel % des Gesamtverbrauchs kam vom Netz (0–100)
            double gridPortion       = total > 0    ? round2(grid / total * 100.0)    : 0.0;

            CurrentPercentage pct = percentageRepository.findByHour(msg.getHour())
                    .orElseGet(() -> new CurrentPercentage(msg.getHour(), 0, 0));

            pct.setCommunityDepleted(communityDepleted);
            pct.setGridPortion(gridPortion);
            percentageRepository.save(pct);

            System.out.printf("Percentage | hour=%s | communityDepleted=%.2f%% | gridPortion=%.2f%%%n",
                    msg.getHour(), communityDepleted, gridPortion);
        });
    }

    private double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}