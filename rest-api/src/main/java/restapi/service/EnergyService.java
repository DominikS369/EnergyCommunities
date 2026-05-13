package restapi.service;

import restapi.entity.CurrentPercentage;
import restapi.entity.EnergyUsage;
import restapi.repository.CurrentPercentageRepository;
import restapi.repository.EnergyUsageRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
public class EnergyService {

    private final EnergyUsageRepository energyUsageRepository;
    private final CurrentPercentageRepository currentPercentageRepository;

    public EnergyService(EnergyUsageRepository energyUsageRepository,
                       CurrentPercentageRepository currentPercentageRepository) {
        this.energyUsageRepository = energyUsageRepository;
        this.currentPercentageRepository = currentPercentageRepository;
    }

    // Aktuelle Prozentsätze abrufen
    public Optional<CurrentPercentage> getCurrentPercentage() {
        LocalDateTime currentHour = LocalDateTime.now().truncatedTo(ChronoUnit.HOURS);
        return currentPercentageRepository.findByHour(currentHour);
    }

    // Historische Daten abrufen
    public List<EnergyUsage> getHistoricalUsage(LocalDateTime start, LocalDateTime end) {
        return energyUsageRepository.findByHourBetween(start, end);
    }
}

