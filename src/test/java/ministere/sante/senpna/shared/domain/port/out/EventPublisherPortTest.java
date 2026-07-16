package ministere.sante.senpna.shared.domain.port.out;

import ministere.sante.senpna.shared.domain.events.DomainEvent;
import ministere.sante.senpna.shared.domain.model.AggregateRoot;
import ministere.sante.senpna.shared.domain.valueobject.UserId;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link EventPublisherPort#publishAndClear(AggregateRoot)} est une méthode
 * par défaut de l'interface : elle est exercée ici via une implémentation
 * de test minimale, plutôt qu'à travers un adaptateur d'infrastructure
 * concret.
 */
@DisplayName("EventPublisherPort — publishAndClear() (méthode par défaut)")
class EventPublisherPortTest {

    private record EvenementTest(Instant occurredAt) implements DomainEvent {
        @Override
        public Instant getOccurredAt() {
            return occurredAt;
        }
    }

    private static final class AgregatTest extends AggregateRoot<UserId> {
        AgregatTest(UserId id) {
            super(id);
        }

        void emettre(DomainEvent event) {
            registerEvent(event);
        }
    }

    private static final class PublisherDeTest implements EventPublisherPort {
        final List<DomainEvent> publies = new ArrayList<>();

        @Override
        public void publish(DomainEvent event) {
            publies.add(event);
        }

        @Override
        public void publishAll(List<DomainEvent> events) {
            publies.addAll(events);
        }
    }

    @Test
    @DisplayName("publie tous les events accumulés puis vide l'agrégat")
    void publishAndClear_publieEtVide() {
        PublisherDeTest publisher = new PublisherDeTest();
        AgregatTest agregat = new AgregatTest(UserId.generate());
        EvenementTest event = new EvenementTest(Instant.now());
        agregat.emettre(event);

        publisher.publishAndClear(agregat);

        assertThat(publisher.publies).containsExactly(event);
        assertThat(agregat.getDomainEvents()).isEmpty();
    }

    @Test
    @DisplayName("agrégat sans event accumulé → publishAll() n'est pas appelé")
    void publishAndClear_sansEvent_neFaitRien() {
        PublisherDeTest publisher = new PublisherDeTest();
        AgregatTest agregat = new AgregatTest(UserId.generate());

        publisher.publishAndClear(agregat);

        assertThat(publisher.publies).isEmpty();
    }
}
