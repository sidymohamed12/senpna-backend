package ministere.sante.senpna.shared.domain.valueobject;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("EntityId — base commune des identifiants typés")
class EntityIdTest {

    private static final class TestId extends EntityId {
        TestId(UUID value) {
            super(value);
        }

        TestId(String value) {
            super(value);
        }
    }

    private static final class AutreTestId extends EntityId {
        AutreTestId(UUID value) {
            super(value);
        }
    }

    @Nested
    @DisplayName("construction")
    class Construction {

        @Test
        @DisplayName("depuis un UUID → getValue() renvoie ce même UUID")
        void depuisUuid() {
            UUID uuid = UUID.randomUUID();
            assertThat(new TestId(uuid).getValue()).isEqualTo(uuid);
        }

        @Test
        @DisplayName("depuis une chaîne valide → parsée en UUID")
        void depuisChaineValide() {
            UUID uuid = UUID.randomUUID();
            assertThat(new TestId(uuid.toString()).getValue()).isEqualTo(uuid);
        }

        @Test
        @DisplayName("depuis une chaîne malformée → IllegalArgumentException")
        void depuisChaineMalformee_leveException() {
            assertThatThrownBy(() -> new TestId("pas-un-uuid")).isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("UUID null → NullPointerException")
        void uuidNull_leveException() {
            assertThatThrownBy(() -> new TestId((UUID) null)).isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("equals() / hashCode()")
    class EqualsHashCode {

        @Test
        @DisplayName("même sous-classe, même UUID → égaux")
        void memeSousClasseMemeUuid_egaux() {
            UUID uuid = UUID.randomUUID();
            assertThat(new TestId(uuid)).isEqualTo(new TestId(uuid));
        }

        @Test
        @DisplayName("sous-classes DIFFÉRENTES avec le même UUID → NON égaux (sécurité de typage)")
        void sousClassesDifferentes_nonEgauxMemeAvecMemeUuid() {
            UUID uuid = UUID.randomUUID();

            Object testId = new TestId(uuid);
            Object auteurId = new AutreTestId(uuid);

            assertThat(testId).isNotEqualTo(auteurId);
        }

        @Test
        @DisplayName("UUID différents → non égaux")
        void uuidDifferents_nonEgaux() {
            assertThat(new TestId(UUID.randomUUID())).isNotEqualTo(new TestId(UUID.randomUUID()));
        }
    }

    @Test
    @DisplayName("toString() renvoie la représentation textuelle de l'UUID")
    void toString_representationUuid() {
        UUID uuid = UUID.randomUUID();
        assertThat(new TestId(uuid)).hasToString(uuid.toString());
    }
}
