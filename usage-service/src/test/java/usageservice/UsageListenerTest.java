package usageservice;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import shared.EnergyMessage;
import shared.UpdateMessage;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the core aggregation logic of the Usage Service.
 * producer vs user, community-first vs grid fallback,
 * hour bucketing, the update signal
 */
@ExtendWith(MockitoExtension.class)
class UsageListenerTest {

    @Mock
    private EnergyUsageRepository repository;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private UsageListener listener;

    private static final double EPS = 1e-6;

    // PRODUCER

    @Test
    void producerMessage_newHour_accumulatesProductionAndPublishesUpdate() {
        LocalDateTime ts = LocalDateTime.of(2026, 6, 23, 14, 37, 12);
        LocalDateTime hour = LocalDateTime.of(2026, 6, 23, 14, 0);
        when(repository.findByHour(hour)).thenReturn(Optional.empty());

        listener.handle(new EnergyMessage("PRODUCER", "COMMUNITY", 2.5, ts));

        EnergyUsage saved = captureSaved();
        assertThat(saved.getHour()).isEqualTo(hour);
        assertThat(saved.getCommunityProduced()).isCloseTo(2.5, within(EPS));
        assertThat(saved.getCommunityUsed()).isCloseTo(0.0, within(EPS));
        assertThat(saved.getGridUsed()).isCloseTo(0.0, within(EPS));

        assertThat(capturePublishedHour()).isEqualTo(hour);
    }

    @Test
    void producerMessage_existingHour_addsToExistingProduction() {
        LocalDateTime hour = LocalDateTime.of(2026, 6, 23, 14, 0);
        EnergyUsage existing = new EnergyUsage(hour);
        existing.setCommunityProduced(5.0);
        when(repository.findByHour(hour)).thenReturn(Optional.of(existing));

        listener.handle(new EnergyMessage("PRODUCER", "COMMUNITY", 2.0, hour));

        assertThat(captureSaved().getCommunityProduced()).isCloseTo(7.0, within(EPS));
    }

    // USER: community-first allocation, grid as remainder

    @Test
    void userMessage_communityHasSurplus_noGridUsed() {
        LocalDateTime hour = LocalDateTime.of(2026, 6, 23, 14, 0);
        EnergyUsage existing = new EnergyUsage(hour);
        existing.setCommunityProduced(10.0);   // plenty available
        when(repository.findByHour(hour)).thenReturn(Optional.of(existing));

        listener.handle(new EnergyMessage("USER", "COMMUNITY", 3.0, hour));

        EnergyUsage saved = captureSaved();
        assertThat(saved.getCommunityUsed()).isCloseTo(3.0, within(EPS));
        assertThat(saved.getGridUsed()).isCloseTo(0.0, within(EPS));
    }

    @Test
    void userMessage_communityPartiallyDepleted_remainderComesFromGrid() {
        LocalDateTime hour = LocalDateTime.of(2026, 6, 23, 14, 0);
        EnergyUsage existing = new EnergyUsage(hour);
        existing.setCommunityProduced(2.0);    // only 2 kWh available, 5 requested
        when(repository.findByHour(hour)).thenReturn(Optional.of(existing));

        listener.handle(new EnergyMessage("USER", "COMMUNITY", 5.0, hour));

        EnergyUsage saved = captureSaved();
        assertThat(saved.getCommunityUsed()).isCloseTo(2.0, within(EPS));
        assertThat(saved.getGridUsed()).isCloseTo(3.0, within(EPS));
    }

    @Test
    void userMessage_noProduction_allFromGrid() {
        LocalDateTime hour = LocalDateTime.of(2026, 6, 23, 14, 0);
        when(repository.findByHour(hour)).thenReturn(Optional.empty()); // fresh hour, produced=0

        listener.handle(new EnergyMessage("USER", "COMMUNITY", 4.0, hour));

        EnergyUsage saved = captureSaved();
        assertThat(saved.getCommunityUsed()).isCloseTo(0.0, within(EPS));
        assertThat(saved.getGridUsed()).isCloseTo(4.0, within(EPS));
    }

    // Cross-cutting behaviour

    @Test
    void message_isBucketedToTheStartOfTheHour_forLookupAndUpdate() {
        LocalDateTime ts = LocalDateTime.of(2026, 6, 23, 9, 59, 59);
        LocalDateTime hour = LocalDateTime.of(2026, 6, 23, 9, 0);
        when(repository.findByHour(hour)).thenReturn(Optional.empty());

        listener.handle(new EnergyMessage("PRODUCER", "COMMUNITY", 1.0, ts));

        verify(repository).findByHour(hour);          // looked up by truncated hour
        assertThat(capturePublishedHour()).isEqualTo(hour); // update carries truncated hour
    }

    @Test
    void unknownType_isIgnored_noPersistenceNoUpdate() {
        LocalDateTime hour = LocalDateTime.of(2026, 6, 23, 14, 0);
        when(repository.findByHour(hour)).thenReturn(Optional.empty());

        listener.handle(new EnergyMessage("GREMLIN", "COMMUNITY", 9.0, hour));

        verify(repository, never()).save(any());
        verifyNoInteractions(rabbitTemplate);
    }

    // helpers

    private EnergyUsage captureSaved() {
        ArgumentCaptor<EnergyUsage> captor = ArgumentCaptor.forClass(EnergyUsage.class);
        verify(repository).save(captor.capture());
        return captor.getValue();
    }

    private LocalDateTime capturePublishedHour() {
        ArgumentCaptor<UpdateMessage> captor = ArgumentCaptor.forClass(UpdateMessage.class);
        verify(rabbitTemplate).convertAndSend(
                eq(RabbitConfig.EXCHANGE), eq(RabbitConfig.UPDATE_KEY), captor.capture());
        return captor.getValue().getHour();
    }
}
