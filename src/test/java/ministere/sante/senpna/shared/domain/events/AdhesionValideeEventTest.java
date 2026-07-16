package ministere.sante.senpna.shared.domain.events;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("AdhesionValideeEvent")
class AdhesionValideeEventTest {

    @Test
    @DisplayName("expose ses composants et implémente DomainEvent")
    void exposeSesComposants() {
        UUID structureId = UUID.randomUUID();
        Instant occurredAt = Instant.now();
        AdhesionValideeEvent event = new AdhesionValideeEvent(structureId, "Hôpital de Dakar", "Diallo", "Awa",
                "awa.diallo@example.com", occurredAt);

        assertThat(event.structureSanitaireId()).isEqualTo(structureId);
        assertThat(event.structureNom()).isEqualTo("Hôpital de Dakar");
        assertThat(event.responsableNom()).isEqualTo("Diallo");
        assertThat(event.responsablePrenom()).isEqualTo("Awa");
        assertThat(event.email()).isEqualTo("awa.diallo@example.com");
        assertThat(event).isInstanceOf(DomainEvent.class);
        assertThat(event.getOccurredAt()).isEqualTo(occurredAt);
    }

    @Test
    @DisplayName("eventType() renvoie le nom simple de la classe")
    void eventType_nomSimpleDeLaClasse() {
        AdhesionValideeEvent event = new AdhesionValideeEvent(UUID.randomUUID(), "Hôpital", "Diallo", "Awa",
                "awa@example.com", Instant.now());

        assertThat(event.eventType()).isEqualTo("AdhesionValideeEvent");
    }
}
