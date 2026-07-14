package ministere.sante.senpna.shared.infrastructure.event;

import ministere.sante.senpna.shared.domain.events.DomainEvent;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import ministere.sante.senpna.shared.infrastructure.event.SpringDomainEventPublisher;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("SpringDomainEventPublisher — publication d'événements de domaine via Spring")
class SpringDomainEventPublisherTest {

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    private SpringDomainEventPublisher sut;

    @BeforeEach
    void setUp() {
        sut = new SpringDomainEventPublisher(applicationEventPublisher);
    }

    private record TestEvent(Instant occurredAt) implements DomainEvent {
        @Override
        public Instant getOccurredAt() {
            return occurredAt;
        }
    }

    @Test
    @DisplayName("publish() délègue à ApplicationEventPublisher")
    void publish_delegue() {
        TestEvent event = new TestEvent(Instant.now());

        sut.publish(event);

        verify(applicationEventPublisher).publishEvent(event);
    }

    @Test
    @DisplayName("publish() avec un événement null lève une NullPointerException")
    void publishNull_leveException() {
        assertThatThrownBy(() -> sut.publish(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("L'event ne peut pas être null");
    }

    @Test
    @DisplayName("publishAll() publie tous les événements")
    void publishAll_publieChaqueEvenement() {
        TestEvent e1 = new TestEvent(Instant.parse("2026-01-01T10:00:00Z"));
        TestEvent e2 = new TestEvent(Instant.parse("2026-01-01T10:00:01Z"));

        sut.publishAll(List.of(e1, e2));

        ArgumentCaptor<DomainEvent> captor = ArgumentCaptor.forClass(DomainEvent.class);

        verify(applicationEventPublisher, times(2))
                .publishEvent(captor.capture());

        assertThat(captor.getAllValues())
                .containsExactly(e1, e2);
    }

    @Test
    @DisplayName("publishAll() avec une liste vide ne publie rien")
    void publishAllListeVide_aucunePublication() {
        sut.publishAll(List.of());

        verify(applicationEventPublisher, never())
                .publishEvent(any());
    }

    @Test
    @DisplayName("publishAll() avec une liste null lève une NullPointerException")
    void publishAllNull_leveException() {
        assertThatThrownBy(() -> sut.publishAll(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("La liste d'events ne peut pas être null");
    }
}