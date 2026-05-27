package restapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import restapi.entity.CurrentPercentage;
import restapi.entity.EnergyUsage;
import restapi.repository.CurrentPercentageRepository;
import restapi.repository.EnergyUsageRepository;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class EnergyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CurrentPercentageRepository currentPercentageRepository;

    @MockitoBean
    private EnergyUsageRepository energyUsageRepository;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    // --- /energy/current ---

    @Test
    void getCurrentPercentage_returnsData() throws Exception {
        LocalDateTime hour = LocalDateTime.now().truncatedTo(ChronoUnit.HOURS);

        CurrentPercentage cp = new CurrentPercentage();
        cp.setHour(hour);
        cp.setCommunityDepleted(45.5);
        cp.setGridPortion(12.3);

        when(currentPercentageRepository.findByHour(any(LocalDateTime.class)))
                .thenReturn(Optional.of(cp));

        mockMvc.perform(get("/energy/current"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.communityDepleted").value(45.5))
                .andExpect(jsonPath("$.gridPortion").value(12.3));
    }

    @Test
    void getCurrentPercentage_noData_returns404() throws Exception {
        when(currentPercentageRepository.findByHour(any(LocalDateTime.class)))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/energy/current"))
                .andExpect(status().isNotFound());
    }

    // --- /energy/historical ---

    @Test
    void getHistoricalUsage_returnsList() throws Exception {
        LocalDateTime hour1 = LocalDateTime.of(2026, 5, 20, 10, 0);
        LocalDateTime hour2 = LocalDateTime.of(2026, 5, 20, 11, 0);

        EnergyUsage u1 = new EnergyUsage();
        u1.setHour(hour1);
        u1.setCommunityProduced(1.5);
        u1.setCommunityUsed(0.8);
        u1.setGridUsed(0.2);

        EnergyUsage u2 = new EnergyUsage();
        u2.setHour(hour2);
        u2.setCommunityProduced(2.0);
        u2.setCommunityUsed(1.0);
        u2.setGridUsed(0.0);

        when(energyUsageRepository.findByHourBetween(any(), any()))
                .thenReturn(List.of(u1, u2));

        mockMvc.perform(get("/energy/historical")
                        .param("start", "2026-05-20T10:00:00")
                        .param("end",   "2026-05-20T12:00:00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].communityProduced").value(1.5))
                .andExpect(jsonPath("$[0].gridUsed").value(0.2))
                .andExpect(jsonPath("$[1].communityProduced").value(2.0));
    }

    @Test
    void getHistoricalUsage_emptyRange_returnsEmptyList() throws Exception {
        when(energyUsageRepository.findByHourBetween(any(), any()))
                .thenReturn(List.of());

        mockMvc.perform(get("/energy/historical")
                        .param("start", "2026-01-01T00:00:00")
                        .param("end",   "2026-01-01T01:00:00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
}