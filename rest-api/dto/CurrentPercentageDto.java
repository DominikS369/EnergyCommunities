package restapi.dto;

import java.time.LocalDateTime;

public record CurrentPercentageDto(
        LocalDateTime hour,
        double communityDepleted,
        double gridPortion
) {
}