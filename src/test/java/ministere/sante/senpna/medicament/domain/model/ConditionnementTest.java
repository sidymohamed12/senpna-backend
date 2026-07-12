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
        @DisplayName("crée un conditionnement unité de base actif, sans prix")
        void creer_uniteBase_succes() {
            Conditionnement conditionnement = Conditionnement.creer(MEDICAMENT_ID, "Comprimé", 1, BigDecimal.ONE,
                    true, null, null);

            assertThat(conditionnement.isActif()).isTrue();
            assertThat(conditionnement.isEstUniteBase()).isTrue();
            assertThat(conditionnement.getQuantiteUniteBase()).isEqualByComparingTo(BigDecimal.ONE);
            assertThat(conditionnement.getNiveau()).isEqualTo(1);
            assertThat(conditionnement.aUnPrix()).isFalse();
        }

        @Test
        @DisplayName("crée un conditionnement de niveau supérieur avec un prix (ex: carton)")
        void creer_niveauSuperieur_avecPrix_succes() {
            Conditionnement carton = Conditionnement.creer(MEDICAMENT_ID, "Carton", 4, new BigDecimal("10000"),
                    false, new BigDecimal("15000"), new BigDecimal("18000"));

            assertThat(carton.isEstUniteBase()).isFalse();
            assertThat(carton.getQuantiteUniteBase()).isEqualByComparingTo(new BigDecimal("10000"));
            assertThat(carton.aUnPrix()).isTrue();
            assertThat(carton.getPrixAchat()).isEqualByComparingTo(new BigDecimal("15000"));
            assertThat(carton.getPrixVente()).isEqualByComparingTo(new BigDecimal("18000"));
        }

        @Test
        @DisplayName("unité de base avec quantité ≠ 1 → IllegalArgumentException")
        void creer_uniteBaseQuantiteInvalide_leveException() {
            var bigDecimal = new BigDecimal("2");
            assertThatThrownBy(() -> Conditionnement.creer(MEDICAMENT_ID, "Comprimé", 1, bigDecimal, true, null, null))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("niveau inférieur à 1 → IllegalArgumentException")
        void creer_niveauInvalide_leveException() {
            assertThatThrownBy(
                    () -> Conditionnement.creer(MEDICAMENT_ID, "Comprimé", 0, BigDecimal.ONE, true, null, null))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("quantité nulle ou négative → IllegalArgumentException")
        void creer_quantiteNegative_leveException() {
            assertThatThrownBy(
                    () -> Conditionnement.creer(MEDICAMENT_ID, "Boîte", 2, BigDecimal.ZERO, false, null, null))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("nom vide → IllegalArgumentException")
        void creer_nomVide_leveException() {
            assertThatThrownBy(
                    () -> Conditionnement.creer(MEDICAMENT_ID, "  ", 1, BigDecimal.ONE, true, null, null))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("medicamentId null → NullPointerException")
        void creer_medicamentIdNull_leveException() {
            assertThatThrownBy(() -> Conditionnement.creer(null, "Comprimé", 1, BigDecimal.ONE, true, null, null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("prix de vente sans prix d'achat → IllegalArgumentException")
        void creer_prixIncomplet_leveException() {
            var bigDecimal = new BigDecimal("20");
            var bigDecimal2 = new BigDecimal("1000");
            assertThatThrownBy(() -> Conditionnement.creer(MEDICAMENT_ID, "Boîte", 2, bigDecimal, false, null, bigDecimal2))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("prix de vente inférieur au prix d'achat → IllegalArgumentException")
        void creer_prixVenteInferieurPrixAchat_leveException() {
            var bigDecimal = new BigDecimal("20");
            var bigDecimal2 = new BigDecimal("1000");
            var bigDecimal3 = new BigDecimal("500");
            assertThatThrownBy(() -> Conditionnement.creer(MEDICAMENT_ID, "Boîte", 2, bigDecimal, false, bigDecimal2, bigDecimal3))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("prix négatif → IllegalArgumentException")
        void creer_prixNegatif_leveException() {
            var bigDecimal = new BigDecimal("20");
            var bigDecimal2 = new BigDecimal("-1");
            var bigDecimal3 = new BigDecimal("500");
            assertThatThrownBy(() -> Conditionnement.creer(MEDICAMENT_ID, "Boîte", 2, bigDecimal, false, bigDecimal2, bigDecimal3))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("convertirVersUniteBase()")
    class ConvertirVersUniteBase {

        @Test
        @DisplayName("convertit correctement une quantité de carton en comprimés")
        void convertir_carton_versComprimes() {
            Conditionnement carton = Conditionnement.creer(MEDICAMENT_ID, "Carton", 4, new BigDecimal("10000"),
                    false, null, null);

            BigDecimal resultat = carton.convertirVersUniteBase(new BigDecimal("20"));

            assertThat(resultat).isEqualByComparingTo(new BigDecimal("200000"));
        }

        @Test
        @DisplayName("quantité négative → IllegalArgumentException")
        void convertir_quantiteNegative_leveException() {
            Conditionnement carton = Conditionnement.creer(MEDICAMENT_ID, "Carton", 4, new BigDecimal("10000"),
                    false, null, null);

            var bigDecimal = new BigDecimal("-1");
            assertThatThrownBy(() -> carton.convertirVersUniteBase(bigDecimal))
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
                    false, null, null);

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
        @DisplayName("met à jour les informations du conditionnement, y compris le prix")
        void modifierInformations_succes() {
            Conditionnement conditionnement = Conditionnement.creer(MEDICAMENT_ID, "Boîte", 2, new BigDecimal("20"),
                    false, null, null);

            conditionnement.modifierInformations("Boîte de 30", 2, new BigDecimal("30"), false,
                    new BigDecimal("1000"), new BigDecimal("1200"));

            assertThat(conditionnement.getNom()).isEqualTo("Boîte de 30");
            assertThat(conditionnement.getQuantiteUniteBase()).isEqualByComparingTo(new BigDecimal("30"));
            assertThat(conditionnement.aUnPrix()).isTrue();
            assertThat(conditionnement.getPrixVente()).isEqualByComparingTo(new BigDecimal("1200"));
        }

        @Test
        @DisplayName("peut retirer le prix en repassant les deux champs à null")
        void modifierInformations_retirePrix_succes() {
            Conditionnement conditionnement = Conditionnement.creer(MEDICAMENT_ID, "Boîte", 2, new BigDecimal("20"),
                    false, new BigDecimal("1000"), new BigDecimal("1200"));

            conditionnement.modifierInformations("Boîte", 2, new BigDecimal("20"), false, null, null);

            assertThat(conditionnement.aUnPrix()).isFalse();
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
                    new BigDecimal("10"), false, new BigDecimal("500"), new BigDecimal("600"), false, createdAt,
                    updatedAt);

            assertThat(conditionnement.getId()).isEqualTo(id);
            assertThat(conditionnement.isActif()).isFalse();
            assertThat(conditionnement.getCreatedAt()).isEqualTo(createdAt);
            assertThat(conditionnement.getPrixVente()).isEqualByComparingTo(new BigDecimal("600"));
        }
    }
}
