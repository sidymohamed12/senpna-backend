package ministere.sante.senpna.organisation.domain.model;

import ministere.sante.senpna.organisation.domain.valueobject.RegionId;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Region — agrégat de domaine")
class RegionTest {

    @Nested
    @DisplayName("creer()")
    class Creer {

        @Test
        @DisplayName("crée une région active, avec le code normalisé en majuscules")
        void creer_succes_regionActive() {
            Region region = Region.creer("thies", "Thiès");

            assertThat(region.isActif()).isTrue();
            assertThat(region.getCode()).isEqualTo("THIES");
            assertThat(region.getNom()).isEqualTo("Thiès");
            assertThat(region.getId()).isNotNull();
        }

        @Test
        @DisplayName("code invalide (caractères non autorisés) → IllegalArgumentException")
        void creer_codeInvalide_leveException() {
            assertThatThrownBy(() -> Region.creer("th!es", "Thiès"))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("code trop court → IllegalArgumentException")
        void creer_codeTropCourt_leveException() {
            assertThatThrownBy(() -> Region.creer("A", "Nom"))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("nom vide → IllegalArgumentException")
        void creer_nomVide_leveException() {
            assertThatThrownBy(() -> Region.creer("DAKAR", "   "))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("nom trop long (> 100 caractères) → IllegalArgumentException")
        void creer_nomTropLong_leveException() {
            String nomLong = "a".repeat(101);

            assertThatThrownBy(() -> Region.creer("DAKAR", nomLong))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("code null → NullPointerException")
        void creer_codeNull_leveException() {
            assertThatThrownBy(() -> Region.creer(null, "Dakar"))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("activer() / desactiver()")
    class ActiverDesactiver {

        @Test
        @DisplayName("desactiver() une région active la rend inactive")
        void desactiver_regionActive_devientInactive() {
            Region region = Region.creer("DAKAR", "Dakar");

            region.desactiver();

            assertThat(region.isActif()).isFalse();
        }

        @Test
        @DisplayName("activer() une région déjà active est sans effet (idempotent)")
        void activer_dejaActive_idempotent() {
            Region region = Region.creer("DAKAR", "Dakar");
            Instant updatedAtInitial = region.getUpdatedAt();

            region.activer();

            assertThat(region.isActif()).isTrue();
            assertThat(region.getUpdatedAt()).isEqualTo(updatedAtInitial);
        }

        @Test
        @DisplayName("desactiver() puis activer() → région de nouveau active")
        void desactiverPuisActiver_redevientActive() {
            Region region = Region.creer("DAKAR", "Dakar");

            region.desactiver();
            region.activer();

            assertThat(region.isActif()).isTrue();
        }
    }

    @Nested
    @DisplayName("renommer()")
    class Renommer {

        @Test
        @DisplayName("renommer() met à jour le nom")
        void renommer_succes_metAJourNom() {
            Region region = Region.creer("DAKAR", "Dakar");

            region.renommer("Région de Dakar");

            assertThat(region.getNom()).isEqualTo("Région de Dakar");
        }

        @Test
        @DisplayName("renommer() avec un nom vide → IllegalArgumentException")
        void renommer_nomVide_leveException() {
            Region region = Region.creer("DAKAR", "Dakar");

            assertThatThrownBy(() -> region.renommer(""))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("reconstruct()")
    class Reconstruct {

        @Test
        @DisplayName("reconstruit fidèlement une région depuis des valeurs de persistance")
        void reconstruct_restaureEtatComplet() {
            RegionId id = RegionId.generate();
            Instant createdAt = Instant.parse("2026-01-01T00:00:00Z");
            Instant updatedAt = Instant.parse("2026-01-02T00:00:00Z");

            Region region = Region.reconstruct(id, "KAOLACK", "Kaolack", false, createdAt, updatedAt);

            assertThat(region.getId()).isEqualTo(id);
            assertThat(region.getCode()).isEqualTo("KAOLACK");
            assertThat(region.isActif()).isFalse();
            assertThat(region.getCreatedAt()).isEqualTo(createdAt);
            assertThat(region.getUpdatedAt()).isEqualTo(updatedAt);
        }
    }
}
