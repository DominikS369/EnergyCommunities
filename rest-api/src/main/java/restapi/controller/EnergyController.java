package restapi.controller;

import restapi.dto.CurrentPercentageDto;
import restapi.dto.HistoricalUsageDto;
import restapi.service.EnergyService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/energy")
public class EnergyController {

    private final EnergyService energyService;

    public EnergyController(EnergyService energyService) {
        this.energyService = energyService;
    }

    @GetMapping("/current")
    public ResponseEntity<CurrentPercentageDto> current() {
        return energyService.getCurrentPercentage()
                .map(cp -> ResponseEntity.ok(new CurrentPercentageDto(
                        cp.getHour(),
                        cp.getCommunityDepleted(),
                        cp.getGridPortion()
                )))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/historical")
    public List<HistoricalUsageDto> historical(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return energyService.getHistoricalUsage(start, end)
                .stream()
                .map(eu -> new HistoricalUsageDto(
                        eu.getHour(),
                        eu.getCommunityProduced(),
                        eu.getCommunityUsed(),
                        eu.getGridUsed()
                ))
                .toList();
    }
}
