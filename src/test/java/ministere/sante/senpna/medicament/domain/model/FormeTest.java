package ministere.sante.senpna.medicament.domain.model;

import ministere.sante.senpna.medicament.domain.valueobject.FormeId;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Forme — agrégat de domaine")
class FormeTest {

    @Nested
    @DisplayName("creer()")
    class Creer {

        @Test
        @DisplayName("crée une forme active, avec le code normalisé en majuscules")
        void creer_succes_formeActive() {
            Forme forme = Forme.creer("comprime", "Comprimé", null);

            assertThat(forme.isActif()).isTrue();
            assertThat(forme.getCode()).isEqualTo("COMPRIME");
            assertThat(forme.getLibelle()).isEqualTo("Comprimé");
            assertThat(forme.getId()).isNotNull();
        }

        @Test
        @DisplayName("code invalide → IllegalArgumentException")
        void creer_codeInvalide_leveException() {
            assertThatThrownBy(() -> Forme.creer("com!prime", "Comprimé", null))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("libellé vide → IllegalArgumentException")
        void creer_libelleVide_leveException() {
            assertThatThrownBy(() -> Forme.creer("COMPRIME", "  ", null))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("archiver() / desarchiver()")
    class ArchiverDesarchiver {

        @Test
        @DisplayName("archiver() puis desarchiver() → forme de nouveau active")
        void archiverPuisDesarchiver_redevientActive() {
            Forme forme = Forme.creer("SIROP", "Sirop", null);

            forme.archiver();
            assertThat(forme.isActif()).isFalse();

            forme.desarchiver();
            assertThat(forme.isActif()).isTrue();
        }
    }

    @Nested
    @DisplayName("modifierInformations()")
    class ModifierInformations {

        @Test
        @DisplayName("met à jour le libellé et la description")
        void modifierInformations_succes() {
            Forme forme = Forme.creer("SIROP", "Sirop", null);

            forme.modifierInformations("Sirop buvable", "Voie orale liquide");

            assertThat(forme.getLibelle()).isEqualTo("Sirop buvable");
            assertThat(forme.getDescription()).isEqualTo("Voie orale liquide");
        }
    }

    @Nested
    @DisplayName("reconstruct()")
    class Reconstruct {

        @Test
        @DisplayName("reconstruit fidèlement une forme depuis des valeurs de persistance")
        void reconstruct_restaureEtatComplet() {
            FormeId id = FormeId.generate();
            Instant createdAt = Instant.parse("2026-01-01T00:00:00Z");
            Instant updatedAt = Instant.parse("2026-01-02T00:00:00Z");

            Forme forme = Forme.reconstruct(id, "POMMADE", "Pommade", null, false, createdAt, updatedAt);

            assertThat(forme.getId()).isEqualTo(id);
            assertThat(forme.isActif()).isFalse();
            assertThat(forme.getCreatedAt()).isEqualTo(createdAt);
        }
    }
}
