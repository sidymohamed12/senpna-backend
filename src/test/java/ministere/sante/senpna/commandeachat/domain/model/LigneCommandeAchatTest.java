package ministere.sante.senpna.commandeachat.domain.model;

import ministere.sante.senpna.medicament.domain.valueobject.ConditionnementId;
import ministere.sante.senpna.medicament.domain.valueobject.MedicamentId;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("LigneCommandeAchat — entité de domaine")
class LigneCommandeAchatTest {

    private LigneCommandeAchat ligne() {
        return LigneCommandeAchat.creer(MedicamentId.generate(), ConditionnementId.generate(), BigDecimal.TEN,
                BigDecimal.valueOf(200_000));
    }

    @Nested
    @DisplayName("creer()")
    class Creer {

        @Test
        @DisplayName("crée une ligne avec quantités reçue/refusée initialisées à zéro")
        void creer_succes() {
            LigneCommandeAchat ligne = ligne();

            assertThat(ligne.getQuantiteCommandee()).isEqualByComparingTo("10");
            assertThat(ligne.getQuantiteRecue()).isEqualByComparingTo("0");
            assertThat(ligne.getQuantiteRefusee()).isEqualByComparingTo("0");
            assertThat(ligne.getNumeroLot()).isNull();
        }

        @Test
        @DisplayName("quantité commandée nulle ou négative → IllegalArgumentException")
        void quantiteInvalide_leveException() {

            var medicamentId = MedicamentId.generate();
            var conditionnementId = ConditionnementId.generate();

            assertThatThrownBy(() -> LigneCommandeAchat.creer(medicamentId, conditionnementId,
                    BigDecimal.ZERO, BigDecimal.TEN)).isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("prix unitaire nul ou négatif → IllegalArgumentException")
        void prixInvalide_leveException() {

            var medicamentId = MedicamentId.generate();
            var conditionnementId = ConditionnementId.generate();

            assertThatThrownBy(() -> LigneCommandeAchat.creer(medicamentId, conditionnementId,
                    BigDecimal.TEN, BigDecimal.ZERO)).isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("renseignerExpedition()")
    class RenseignerExpedition {

        @Test
        @DisplayName("renseigne le lot et la quantité expédiée")
        void succes() {
            LigneCommandeAchat ligne = ligne();

            ligne.renseignerExpedition("LOT-A001", LocalDate.now(), LocalDate.now().plusYears(2), "url",
                    BigDecimal.TEN);

            assertThat(ligne.getNumeroLot()).isEqualTo("LOT-A001");
            assertThat(ligne.getQuantiteExpediee()).isEqualByComparingTo("10");
        }

        @Test
        @DisplayName("date de fabrication après la date de péremption → IllegalArgumentException")
        void dateFabricationApresExpiration_leveException() {
            LigneCommandeAchat ligne = ligne();
            var now = LocalDate.now();
            var nowPlus10Days = LocalDate.now().plusDays(10);

            assertThatThrownBy(() -> ligne.renseignerExpedition("LOT-A001", nowPlus10Days,
                    now, null, BigDecimal.TEN)).isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("quantité expédiée supérieure à la quantité commandée → IllegalArgumentException")
        void quantiteExpedieeExcessive_leveException() {
            LigneCommandeAchat ligne = ligne();
            var nowPlus1Year = LocalDate.now().plusYears(1);
            var quantiteExpediee = BigDecimal.valueOf(11);

            assertThatThrownBy(() -> ligne.renseignerExpedition("LOT-A001", null, nowPlus1Year, null,
                    quantiteExpediee)).isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("numéro de lot manquant → NullPointerException")
        void numeroLotManquant_leveException() {
            LigneCommandeAchat ligne = ligne();
            var nowPlus1Year = LocalDate.now().plusYears(1);
            var quantiteExpediee = BigDecimal.TEN;

            assertThatThrownBy(() -> ligne.renseignerExpedition(null, null, nowPlus1Year, null,
                    quantiteExpediee)).isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("receptionner()")
    class Receptionner {

        @Test
        @DisplayName("réception totale sans expédition préalable → estCompletementReceptionnee() = true")
        void receptionTotale_completementReceptionnee() {
            LigneCommandeAchat ligne = ligne();

            ligne.receptionner(BigDecimal.TEN, BigDecimal.ZERO, null);

            assertThat(ligne.estCompletementReceptionnee()).isTrue();
            assertThat(ligne.getQuantiteRecue()).isEqualByComparingTo("10");
        }

        @Test
        @DisplayName("réception partielle → estCompletementReceptionnee() = false")
        void receptionPartielle_nonComplete() {
            LigneCommandeAchat ligne = ligne();

            ligne.receptionner(BigDecimal.valueOf(6), BigDecimal.ZERO, null);

            assertThat(ligne.estCompletementReceptionnee()).isFalse();
            assertThat(ligne.getQuantiteRecue()).isEqualByComparingTo("6");
        }

        @Test
        @DisplayName("cumul reçu + refusé = expédié → complètement réceptionnée")
        void cumulRecuEtRefuse_completementReceptionnee() {
            LigneCommandeAchat ligne = ligne();
            ligne.renseignerExpedition("LOT-A001", null, LocalDate.now().plusYears(1), null, BigDecimal.TEN);

            ligne.receptionner(BigDecimal.valueOf(7), BigDecimal.valueOf(3), "Casse au transport");

            assertThat(ligne.estCompletementReceptionnee()).isTrue();
            assertThat(ligne.getMotifRefus()).isEqualTo("Casse au transport");
        }

        @Test
        @DisplayName("réception cumulée dépassant la quantité expédiée → IllegalArgumentException")
        void receptionExcessive_leveException() {
            LigneCommandeAchat ligne = ligne();
            ligne.renseignerExpedition("LOT-A001", null, LocalDate.now().plusYears(1), null, BigDecimal.valueOf(5));

            var qteRecue = BigDecimal.valueOf(6);
            var qteRefuse = BigDecimal.ZERO;
            assertThatThrownBy(() -> ligne.receptionner(qteRecue, qteRefuse, null))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("réceptions successives se cumulent correctement")
        void receptionsSuccessives_seCumulent() {
            LigneCommandeAchat ligne = ligne();

            ligne.receptionner(BigDecimal.valueOf(4), BigDecimal.ZERO, null);
            ligne.receptionner(BigDecimal.valueOf(6), BigDecimal.ZERO, null);

            assertThat(ligne.getQuantiteRecue()).isEqualByComparingTo("10");
            assertThat(ligne.estCompletementReceptionnee()).isTrue();
        }

        @Test
        @DisplayName("quantité négative → IllegalArgumentException")
        void quantiteNegative_leveException() {
            LigneCommandeAchat ligne = ligne();
            var qteRecue = BigDecimal.valueOf(-1);
            var qteRefuse = BigDecimal.ZERO;

            assertThatThrownBy(() -> ligne.receptionner(qteRecue, qteRefuse, null))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }
}
