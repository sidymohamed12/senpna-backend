package ministere.sante.senpna.medicament.domain.model;

import ministere.sante.senpna.medicament.domain.valueobject.FamilleId;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Famille — agrégat de domaine")
class FamilleTest {

    @Nested
    @DisplayName("creer()")
    class Creer {

        @Test
        @DisplayName("crée une famille active, avec le code normalisé en majuscules")
        void creer_succes_familleActive() {
            Famille famille = Famille.creer("antibiotique", "Antibiotique", "Anti-infectieux");

            assertThat(famille.isActif()).isTrue();
            assertThat(famille.getCode()).isEqualTo("ANTIBIOTIQUE");
            assertThat(famille.getLibelle()).isEqualTo("Antibiotique");
            assertThat(famille.getDescription()).isEqualTo("Anti-infectieux");
            assertThat(famille.getId()).isNotNull();
        }

        @Test
        @DisplayName("description absente (null) est acceptée")
        void creer_sansDescription_accepte() {
            Famille famille = Famille.creer("ANTALGIQUE", "Antalgique", null);

            assertThat(famille.getDescription()).isNull();
        }

        @Test
        @DisplayName("code invalide (caractères non autorisés) → IllegalArgumentException")
        void creer_codeInvalide_leveException() {
            assertThatThrownBy(() -> Famille.creer("anti!biotique", "Antibiotique", null))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("libellé vide → IllegalArgumentException")
        void creer_libelleVide_leveException() {
            assertThatThrownBy(() -> Famille.creer("ANTIBIOTIQUE", "   ", null))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("code null → NullPointerException")
        void creer_codeNull_leveException() {
            assertThatThrownBy(() -> Famille.creer(null, "Antibiotique", null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("archiver() / desarchiver()")
    class ArchiverDesarchiver {

        @Test
        @DisplayName("archiver() une famille active la rend inactive")
        void archiver_familleActive_devientInactive() {
            Famille famille = Famille.creer("ANTALGIQUE", "Antalgique", null);

            famille.archiver();

            assertThat(famille.isActif()).isFalse();
        }

        @Test
        @DisplayName("archiver() déjà archivée est sans effet (idempotent)")
        void archiver_dejaArchivee_idempotent() {
            Famille famille = Famille.creer("ANTALGIQUE", "Antalgique", null);
            famille.archiver();
            Instant updatedAtApresArchivage = famille.getUpdatedAt();

            famille.archiver();

            assertThat(famille.isActif()).isFalse();
            assertThat(famille.getUpdatedAt()).isEqualTo(updatedAtApresArchivage);
        }

        @Test
        @DisplayName("archiver() puis desarchiver() → famille de nouveau active")
        void archiverPuisDesarchiver_redevientActive() {
            Famille famille = Famille.creer("ANTALGIQUE", "Antalgique", null);

            famille.archiver();
            famille.desarchiver();

            assertThat(famille.isActif()).isTrue();
        }
    }

    @Nested
    @DisplayName("modifierInformations()")
    class ModifierInformations {

        @Test
        @DisplayName("met à jour le libellé et la description, sans toucher au code")
        void modifierInformations_succes() {
            Famille famille = Famille.creer("ANTALGIQUE", "Antalgique", null);

            famille.modifierInformations("Antalgique majeur", "Douleurs sévères");

            assertThat(famille.getLibelle()).isEqualTo("Antalgique majeur");
            assertThat(famille.getDescription()).isEqualTo("Douleurs sévères");
            assertThat(famille.getCode()).isEqualTo("ANTALGIQUE");
        }

        @Test
        @DisplayName("libellé vide → IllegalArgumentException")
        void modifierInformations_libelleVide_leveException() {
            Famille famille = Famille.creer("ANTALGIQUE", "Antalgique", null);

            assertThatThrownBy(() -> famille.modifierInformations("", null))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("reconstruct()")
    class Reconstruct {

        @Test
        @DisplayName("reconstruit fidèlement une famille depuis des valeurs de persistance")
        void reconstruct_restaureEtatComplet() {
            FamilleId id = FamilleId.generate();
            Instant createdAt = Instant.parse("2026-01-01T00:00:00Z");
            Instant updatedAt = Instant.parse("2026-01-02T00:00:00Z");

            Famille famille = Famille.reconstruct(id, "ANTIPALUDEEN", "Antipaludéen", null, false, createdAt,
                    updatedAt);

            assertThat(famille.getId()).isEqualTo(id);
            assertThat(famille.getCode()).isEqualTo("ANTIPALUDEEN");
            assertThat(famille.isActif()).isFalse();
            assertThat(famille.getCreatedAt()).isEqualTo(createdAt);
            assertThat(famille.getUpdatedAt()).isEqualTo(updatedAt);
        }
    }
}
