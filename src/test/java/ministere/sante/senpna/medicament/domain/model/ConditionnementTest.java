package ministere.sante.senpna.medicament.domain.model;

import ministere.sante.senpna.medicament.domain.valueobject.ConditionnementId;
import ministere.sante.senpna.medicament.domain.valueobject.MedicamentId;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Conditionnement — agrégat de domaine")
class ConditionnementTest {

    private static final MedicamentId MEDICAMENT_ID = MedicamentId.generate();

    @Nested
    @DisplayName("creer()")
    class Creer {

        @Test
        @DisplayName("crée un conditionnement unité de base actif")
        void creer_uniteBase_succes() {
            Conditionnement conditionnement = Conditionnement.creer(MEDICAMENT_ID, "Comprimé", 1, BigDecimal.ONE,
                    true);

            assertThat(conditionnement.isActif()).isTrue();
            assertThat(conditionnement.isEstUniteBase()).isTrue();
            assertThat(conditionnement.getQuantiteUniteBase()).isEqualByComparingTo(BigDecimal.ONE);
            assertThat(conditionnement.getNiveau()).isEqualTo(1);
        }

        @Test
        @DisplayName("crée un conditionnement de niveau supérieur (ex: carton)")
        void creer_niveauSuperieur_succes() {
            Conditionnement carton = Conditionnement.creer(MEDICAMENT_ID, "Carton", 4, new BigDecimal("10000"),
                    false);

            assertThat(carton.isEstUniteBase()).isFalse();
            assertThat(carton.getQuantiteUniteBase()).isEqualByComparingTo(new BigDecimal("10000"));
        }

        @Test
        @DisplayName("unité de base avec quantité ≠ 1 → IllegalArgumentException")
        void creer_uniteBaseQuantiteInvalide_leveException() {
            assertThatThrownBy(() -> Conditionnement.creer(MEDICAMENT_ID, "Comprimé", 1, new BigDecimal("2"), true))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("niveau inférieur à 1 → IllegalArgumentException")
        void creer_niveauInvalide_leveException() {
            assertThatThrownBy(() -> Conditionnement.creer(MEDICAMENT_ID, "Comprimé", 0, BigDecimal.ONE, true))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("quantité nulle ou négative → IllegalArgumentException")
        void creer_quantiteNegative_leveException() {
            assertThatThrownBy(() -> Conditionnement.creer(MEDICAMENT_ID, "Boîte", 2, BigDecimal.ZERO, false))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("nom vide → IllegalArgumentException")
        void creer_nomVide_leveException() {
            assertThatThrownBy(
                    () -> Conditionnement.creer(MEDICAMENT_ID, "  ", 1, BigDecimal.ONE, true))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("medicamentId null → NullPointerException")
        void creer_medicamentIdNull_leveException() {
            assertThatThrownBy(() -> Conditionnement.creer(null, "Comprimé", 1, BigDecimal.ONE, true))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("convertirVersUniteBase()")
    class ConvertirVersUniteBase {

        @Test
        @DisplayName("convertit correctement une quantité de carton en comprimés")
        void convertir_carton_versComprimes() {
            Conditionnement carton = Conditionnement.creer(MEDICAMENT_ID, "Carton", 4, new BigDecimal("10000"),
                    false);

            BigDecimal resultat = carton.convertirVersUniteBase(new BigDecimal("20"));

            assertThat(resultat).isEqualByComparingTo(new BigDecimal("200000"));
        }

        @Test
        @DisplayName("quantité négative → IllegalArgumentException")
        void convertir_quantiteNegative_leveException() {
            Conditionnement carton = Conditionnement.creer(MEDICAMENT_ID, "Carton", 4, new BigDecimal("10000"),
                    false);

            assertThatThrownBy(() -> carton.convertirVersUniteBase(new BigDecimal("-1")))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("archiver() / desarchiver()")
    class ArchiverDesarchiver {

        @Test
        @DisplayName("archiver() puis desarchiver() → conditionnement de nouveau actif")
        void archiverPuisDesarchiver_redevientActif() {
            Conditionnement conditionnement = Conditionnement.creer(MEDICAMENT_ID, "Boîte", 2, new BigDecimal("20"),
                    false);

            conditionnement.archiver();
            assertThat(conditionnement.isActif()).isFalse();

            conditionnement.desarchiver();
            assertThat(conditionnement.isActif()).isTrue();
        }
    }

    @Nested
    @DisplayName("modifierInformations()")
    class ModifierInformations {

        @Test
        @DisplayName("met à jour les informations du conditionnement")
        void modifierInformations_succes() {
            Conditionnement conditionnement = Conditionnement.creer(MEDICAMENT_ID, "Boîte", 2, new BigDecimal("20"),
                    false);

            conditionnement.modifierInformations("Boîte de 30", 2, new BigDecimal("30"), false);

            assertThat(conditionnement.getNom()).isEqualTo("Boîte de 30");
            assertThat(conditionnement.getQuantiteUniteBase()).isEqualByComparingTo(new BigDecimal("30"));
        }
    }

    @Nested
    @DisplayName("reconstruct()")
    class Reconstruct {

        @Test
        @DisplayName("reconstruit fidèlement un conditionnement depuis des valeurs de persistance")
        void reconstruct_restaureEtatComplet() {
            ConditionnementId id = ConditionnementId.generate();
            Instant createdAt = Instant.parse("2026-01-01T00:00:00Z");
            Instant updatedAt = Instant.parse("2026-01-02T00:00:00Z");

            Conditionnement conditionnement = Conditionnement.reconstruct(id, MEDICAMENT_ID, "Plaquette", 2,
                    new BigDecimal("10"), false, false, createdAt, updatedAt);

            assertThat(conditionnement.getId()).isEqualTo(id);
            assertThat(conditionnement.isActif()).isFalse();
            assertThat(conditionnement.getCreatedAt()).isEqualTo(createdAt);
        }
    }
}
