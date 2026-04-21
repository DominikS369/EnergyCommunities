package restapi.controller;

import restapi.dto.CurrentPercentageDto;
import restapi.dto.HistoricalUsageDto;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@RestController
@RequestMapping("/energy")
public class EnergyController {

    private static final List<HistoricalUsageDto> SAMPLE_HISTORY = List.of(
            new HistoricalUsageDto(LocalDateTime.of(2026, 4, 19,  8, 0), 12.40, 11.80, 0.60),
            new HistoricalUsageDto(LocalDateTime.of(2026, 4, 19, 12, 0), 18.05, 18.05, 1.07),
            new HistoricalUsageDto(LocalDateTime.of(2026, 4, 19, 18, 0), 15.02, 14.03, 2.05),
            new HistoricalUsageDto(LocalDateTime.of(2026, 4, 20,  8, 0), 13.10, 12.95, 0.80),
            new HistoricalUsageDto(LocalDateTime.of(2026, 4, 20, 12, 0), 20.50, 19.90, 0.95)
    );

    @GetMapping("/current")
    public CurrentPercentageDto current() {
        LocalDateTime hour = LocalDateTime.now().truncatedTo(ChronoUnit.HOURS);
        return new CurrentPercentageDto(hour, 100.00, 5.63);
    }

    @GetMapping("/historical")
    public List<HistoricalUsageDto> historical(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return SAMPLE_HISTORY.stream()
                .filter(h -> !h.hour().isBefore(start) && !h.hour().isAfter(end))
                .toList();
    }
}
