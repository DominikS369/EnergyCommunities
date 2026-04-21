package restapi.dto;

import java.time.LocalDateTime;

public record HistoricalUsageDto(
        LocalDateTime hour,
        double communityProduced,
        double communityUsed,
        double gridUsed
) {
}
