package ministere.sante.senpna.shared.domain.model;

import ministere.sante.senpna.shared.domain.events.DomainEvent;
import ministere.sante.senpna.shared.domain.valueobject.UserId;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * {@link BaseEntity} et {@link AggregateRoot} sont abstraites : leurs
 * règles communes (identité, horodatage, gestion des Domain Events) sont
 * exercées ici via des doublures de test minimales, plutôt qu'à travers un
 * agrégat métier concret (déjà couvert ailleurs, mais pas nécessairement
 * sur toutes les branches communes).
 */
@DisplayName("BaseEntity / AggregateRoot — via doublures de test")
class BaseEntityAggregateRootTest {

    private static final class EntiteTest extends BaseEntity<UserId> {
        EntiteTest(UserId id) {
            super(id);
        }

        EntiteTest(UserId id, Instant createdAt, Instant updatedAt) {
            super(id, createdAt, updatedAt);
        }

        void declencherMarkUpdated() {
            markUpdated();
        }
    }

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

    @Nested
    @DisplayName("BaseEntity")
    class BaseEntityTests {

        @Test
        @DisplayName("constructeur (id) → createdAt == updatedAt à l'instanciation")
        void constructeurId_createdEqualsUpdated() {
            EntiteTest entite = new EntiteTest(UserId.generate());

            assertThat(entite.getCreatedAt()).isEqualTo(entite.getUpdatedAt());
        }

        @Test
        @DisplayName("id null → NullPointerException")
        void idNull_leveNpe() {
            assertThatThrownBy(() -> new EntiteTest(null)).isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("constructeur (id, createdAt, updatedAt) → conserve les valeurs fournies")
        void constructeurComplet_conserveLesValeurs() {
            Instant createdAt = Instant.now().minusSeconds(3600);
            Instant updatedAt = Instant.now();
            EntiteTest entite = new EntiteTest(UserId.generate(), createdAt, updatedAt);

            assertThat(entite.getCreatedAt()).isEqualTo(createdAt);
            assertThat(entite.getUpdatedAt()).isEqualTo(updatedAt);
        }

        @Test
        @DisplayName("constructeur (id, createdAt, updatedAt) — createdAt null → NullPointerException")
        void constructeurComplet_createdAtNull_leveNpe() {
            var userId = UserId.generate();
            var now = Instant.now();
            assertThatThrownBy(() -> new EntiteTest(userId, null, now))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("constructeur (id, createdAt, updatedAt) — updatedAt null → NullPointerException")
        void constructeurComplet_updatedAtNull_leveNpe() {
            var userId = UserId.generate();
            var now = Instant.now();
            assertThatThrownBy(() -> new EntiteTest(userId, now, null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("markUpdated() met à jour updatedAt")
        void markUpdated_metAJourUpdatedAt() {
            EntiteTest entite = new EntiteTest(
                    UserId.generate(),
                    Instant.now().minusSeconds(3600),
                    Instant.now().minusSeconds(3600));

            Instant avant = entite.getUpdatedAt();

            entite.declencherMarkUpdated();

            assertThat(entite.getUpdatedAt()).isNotEqualTo(avant);
        }

        @Test
        @DisplayName("equals() — même référence → true")
        void equals_memeReference_true() {
            EntiteTest entite = new EntiteTest(UserId.generate());

            assertThat(entite.equals(entite)).isTrue();
        }

        @Test
        @DisplayName("equals() — comparé à null → false")
        void equals_null_false() {
            assertThat(new EntiteTest(UserId.generate()).equals(null)).isFalse();
        }

        @Test
        @DisplayName("equals() — classe différente → false")
        void equals_classeDifferente_false() {
            assertThat(new EntiteTest(UserId.generate()).equals(new EntiteTest(UserId.generate())))
                    .isFalse();
        }

        @Test
        @DisplayName("equals() — même id → true, quels que soient createdAt/updatedAt")
        void equals_memeId_true() {
            UserId id = UserId.generate();

            assertThat(new EntiteTest(id, Instant.now().minusSeconds(100), Instant.now()))
                    .isEqualTo(new EntiteTest(id, Instant.now().minusSeconds(999), Instant.now().plusSeconds(999)));
        }

        @Test
        @DisplayName("equals() — id différents → false")
        void equals_idDifferents_false() {
            assertThat(new EntiteTest(UserId.generate())).isNotEqualTo(new EntiteTest(UserId.generate()));
        }

        @Test
        @DisplayName("hashCode() — cohérent pour deux instances de même id")
        void hashCode_coherentPourMemeId() {
            UserId id = UserId.generate();

            assertThat(new EntiteTest(id)).hasSameHashCodeAs(new EntiteTest(id));
        }
    }

    @Nested
    @DisplayName("AggregateRoot")
    class AggregateRootTests {

        @Test
        @DisplayName("getDomainEvents() est vide tant qu'aucun event n'a été enregistré")
        void getDomainEvents_videParDefaut() {
            AgregatTest agregat = new AgregatTest(UserId.generate());

            assertThat(agregat.getDomainEvents()).isEmpty();
        }

        @Test
        @DisplayName("registerEvent() accumule les events enregistrés")
        void registerEvent_accumuleLesEvents() {
            AgregatTest agregat = new AgregatTest(UserId.generate());
            EvenementTest event = new EvenementTest(Instant.now());

            agregat.emettre(event);

            assertThat(agregat.getDomainEvents()).containsExactly(event);
        }

        @Test
        @DisplayName("registerEvent(null) est ignoré silencieusement")
        void registerEvent_nullIgnore() {
            AgregatTest agregat = new AgregatTest(UserId.generate());

            agregat.emettre(null);

            assertThat(agregat.getDomainEvents()).isEmpty();
        }

        @Test
        @DisplayName("getDomainEvents() renvoie une vue non modifiable")
        void getDomainEvents_nonModifiable() {
            AgregatTest agregat = new AgregatTest(UserId.generate());
            agregat.emettre(new EvenementTest(Instant.now()));

            var eventTest = new EvenementTest(Instant.now());
            var domainEvents = agregat.getDomainEvents();

            assertThatThrownBy(() -> domainEvents.add(eventTest))
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("clearDomainEvents() vide la liste des events accumulés")
        void clearDomainEvents_videLaListe() {
            AgregatTest agregat = new AgregatTest(UserId.generate());
            agregat.emettre(new EvenementTest(Instant.now()));

            agregat.clearDomainEvents();

            assertThat(agregat.getDomainEvents()).isEmpty();
        }

        @Test
        @DisplayName("equals()/hashCode() délèguent à BaseEntity (identité par id)")
        void equalsEtHashCode_delegueALIdentite() {
            UserId id = UserId.generate();

            assertThat(new AgregatTest(id)).isEqualTo(new AgregatTest(id));
            assertThat(new AgregatTest(id)).hasSameHashCodeAs(new AgregatTest(id));
        }
    }
}
