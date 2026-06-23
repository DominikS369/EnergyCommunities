package percentageservice;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import shared.UpdateMessage;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the Current Percentage Service.
 * Verifies the two computed percentages, the divide-by-zero guards, rounding,
 * and the upsert-by-hour behaviour.
 */
@ExtendWith(MockitoExtension.class)
class PercentageListenerTest {

    @Mock
    private EnergyUsageRepository usageRepository;

    @Mock
    private CurrentPercentageRepository percentageRepository;

    @InjectMocks
    private PercentageListener listener;

    private static final LocalDateTime HOUR = LocalDateTime.of(2026, 6, 23, 14, 0);
    private static final double EPS = 1e-6;

    @Test
    void noUsageForHour_doesNothing() {
        when(usageRepository.findByHour(HOUR)).thenReturn(Optional.empty());

        listener.handle(new UpdateMessage(HOUR));

        verify(percentageRepository, never()).save(any());
        verify(percentageRepository, never()).findByHour(any());
    }

    @Test
    void computesDepletedAndGridPortion_andSavesNewRow() {
        // produced 10, used 4, grid 1 -> depleted = 4/10 = 40%, gridPortion = 1/(4+1) = 20%
        when(usageRepository.findByHour(HOUR)).thenReturn(Optional.of(usage(10, 4, 1)));
        when(percentageRepository.findByHour(HOUR)).thenReturn(Optional.empty());

        listener.handle(new UpdateMessage(HOUR));

        CurrentPercentage saved = captureSaved();
        assertThat(saved.getHour()).isEqualTo(HOUR);
        assertThat(saved.getCommunityDepleted()).isCloseTo(40.0, within(EPS));
        assertThat(saved.getGridPortion()).isCloseTo(20.0, within(EPS));
    }

    @Test
    void zeroProduction_depletedIsZero_noDivisionByZero() {
        // produced 0 -> depleted guard returns 0; total = 0+2 = 2 -> gridPortion = 2/2 = 100%
        when(usageRepository.findByHour(HOUR)).thenReturn(Optional.of(usage(0, 0, 2)));
        when(percentageRepository.findByHour(HOUR)).thenReturn(Optional.empty());

        listener.handle(new UpdateMessage(HOUR));

        CurrentPercentage saved = captureSaved();
        assertThat(saved.getCommunityDepleted()).isCloseTo(0.0, within(EPS));
        assertThat(saved.getGridPortion()).isCloseTo(100.0, within(EPS));
    }

    @Test
    void zeroConsumption_gridPortionIsZero_noDivisionByZero() {
        // produced 5, used 0, grid 0 -> total 0 -> gridPortion guard returns 0; depleted = 0/5 = 0
        when(usageRepository.findByHour(HOUR)).thenReturn(Optional.of(usage(5, 0, 0)));
        when(percentageRepository.findByHour(HOUR)).thenReturn(Optional.empty());

        listener.handle(new UpdateMessage(HOUR));

        CurrentPercentage saved = captureSaved();
        assertThat(saved.getCommunityDepleted()).isCloseTo(0.0, within(EPS));
        assertThat(saved.getGridPortion()).isCloseTo(0.0, within(EPS));
    }

    @Test
    void roundsBothPercentagesToTwoDecimals() {
        // produced 3, used 1, grid 2 -> depleted = 1/3 = 33.33%, gridPortion = 2/3 = 66.67%
        when(usageRepository.findByHour(HOUR)).thenReturn(Optional.of(usage(3, 1, 2)));
        when(percentageRepository.findByHour(HOUR)).thenReturn(Optional.empty());

        listener.handle(new UpdateMessage(HOUR));

        CurrentPercentage saved = captureSaved();
        assertThat(saved.getCommunityDepleted()).isCloseTo(33.33, within(EPS));
        assertThat(saved.getGridPortion()).isCloseTo(66.67, within(EPS));
    }

    @Test
    void existingPercentageRow_isUpdatedInPlace_notDuplicated() {
        CurrentPercentage existing = new CurrentPercentage(HOUR, 99.0, 99.0); // stale values
        when(usageRepository.findByHour(HOUR)).thenReturn(Optional.of(usage(10, 4, 1)));
        when(percentageRepository.findByHour(HOUR)).thenReturn(Optional.of(existing));

        listener.handle(new UpdateMessage(HOUR));

        verify(percentageRepository).save(existing); // same instance, no second row
        assertThat(existing.getCommunityDepleted()).isCloseTo(40.0, within(EPS));
        assertThat(existing.getGridPortion()).isCloseTo(20.0, within(EPS));
    }

    // helpers

    // EnergyUsage is a read-only entity, populate fields directly.
    private EnergyUsage usage(double produced, double used, double grid) {
        EnergyUsage u = new EnergyUsage();
        ReflectionTestUtils.setField(u, "communityProduced", produced);
        ReflectionTestUtils.setField(u, "communityUsed", used);
        ReflectionTestUtils.setField(u, "gridUsed", grid);
        return u;
    }

    private CurrentPercentage captureSaved() {
        ArgumentCaptor<CurrentPercentage> captor = ArgumentCaptor.forClass(CurrentPercentage.class);
        verify(percentageRepository).save(captor.capture());
        return captor.getValue();
    }
}
