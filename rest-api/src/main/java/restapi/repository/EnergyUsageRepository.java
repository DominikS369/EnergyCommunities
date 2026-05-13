package restapi.repository;

import restapi.entity.EnergyUsage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EnergyUsageRepository extends JpaRepository<EnergyUsage, Long> {
    Optional<EnergyUsage> findByHour(LocalDateTime hour);
    List<EnergyUsage> findByHourBetween(LocalDateTime start, LocalDateTime end);
}

