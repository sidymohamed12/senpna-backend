package ministere.sante.senpna.stock.domain.model;

import ministere.sante.senpna.medicament.domain.valueobject.MedicamentId;
import ministere.sante.senpna.organisation.domain.valueobject.EntrepotId;
import ministere.sante.senpna.stock.domain.exception.mouvement.MouvementStockInvalideException;
import ministere.sante.senpna.stock.domain.valueobject.LotId;
import ministere.sante.senpna.stock.domain.valueobject.SensMouvement;
import ministere.sante.senpna.stock.domain.valueobject.TypeMouvement;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("MouvementStock — agrégat de domaine")
class MouvementStockTest {

    private static final EntrepotId ENTREPOT_ID = EntrepotId.generate();
    private static final EntrepotId AUTRE_ENTREPOT_ID = EntrepotId.generate();
    private static final LotId LOT_ID = LotId.generate();
    private static final MedicamentId MEDICAMENT_ID = MedicamentId.generate();
    private static final UUID UTILISATEUR_ID = UUID.randomUUID();

    @Nested
    @DisplayName("creer()")
    class Creer {

        @Test
        @DisplayName("entrée valide avec entrepôt destination")
        void creer_entree_succes() {
            MouvementStock mouvement = MouvementStock.creer(new MouvementStock.CreationCommand(TypeMouvement.ENTREE_ACHAT, SensMouvement.ENTREE, null,
                    ENTREPOT_ID, null, LOT_ID, MEDICAMENT_ID, new BigDecimal("100"), "BC-2026-001", null,
                    UTILISATEUR_ID));

            assertThat(mouvement.getSens()).isEqualTo(SensMouvement.ENTREE);
            assertThat(mouvement.getEntrepotDestinationId()).isEqualTo(ENTREPOT_ID);
            assertThat(mouvement.getQuantite()).isEqualByComparingTo(new BigDecimal("100"));
        }

        @Test
        @DisplayName("sortie valide avec entrepôt source")
        void creer_sortie_succes() {
            MouvementStock mouvement = MouvementStock.creer(new MouvementStock.CreationCommand(TypeMouvement.PERTE, SensMouvement.SORTIE, ENTREPOT_ID,
                    null, null, LOT_ID, MEDICAMENT_ID, new BigDecimal("5"), null, "Casse pendant manutention",
                    UTILISATEUR_ID));

            assertThat(mouvement.getSens()).isEqualTo(SensMouvement.SORTIE);
            assertThat(mouvement.getEntrepotSourceId()).isEqualTo(ENTREPOT_ID);
        }

        @Test
        @DisplayName("transfert valide avec entrepôts source et destination")
        void creer_transfert_succes() {
            MouvementStock mouvement = MouvementStock.creer(new MouvementStock.CreationCommand(TypeMouvement.SORTIE_TRANSFERT, SensMouvement.SORTIE,
                    ENTREPOT_ID, AUTRE_ENTREPOT_ID, null, LOT_ID, MEDICAMENT_ID, new BigDecimal("10"), null, null,
                    UTILISATEUR_ID));

            assertThat(mouvement.getEntrepotSourceId()).isEqualTo(ENTREPOT_ID);
            assertThat(mouvement.getEntrepotDestinationId()).isEqualTo(AUTRE_ENTREPOT_ID);
        }

        @Test
        @DisplayName("transfert sans entrepôt destination → MouvementStockInvalideException")
        void creer_transfertSansDestination_leveException() {
            var bigDecimal = new BigDecimal("10");
            assertThatThrownBy(() -> MouvementStock.creer(new MouvementStock.CreationCommand(TypeMouvement.SORTIE_TRANSFERT, SensMouvement.SORTIE, ENTREPOT_ID, null, null, LOT_ID, MEDICAMENT_ID, bigDecimal, null, null, UTILISATEUR_ID)))
                    .isInstanceOf(MouvementStockInvalideException.class);
        }

        @Test
        @DisplayName("sortie sans entrepôt source → MouvementStockInvalideException")
        void creer_sortieSansSource_leveException() {
            var bigDecimal = new BigDecimal("1");
            assertThatThrownBy(() -> MouvementStock.creer(new MouvementStock.CreationCommand(TypeMouvement.CASSE, SensMouvement.SORTIE, null, null, null, LOT_ID, MEDICAMENT_ID, bigDecimal, null, null, UTILISATEUR_ID)))
                    .isInstanceOf(MouvementStockInvalideException.class);
        }

        @Test
        @DisplayName("entrée sans entrepôt destination → MouvementStockInvalideException")
        void creer_entreeSansDestination_leveException() {
            var bigDecimal = new BigDecimal("1");
            assertThatThrownBy(() -> MouvementStock.creer(new MouvementStock.CreationCommand(TypeMouvement.ENTREE_ACHAT, SensMouvement.ENTREE, null, null, null, LOT_ID, MEDICAMENT_ID, bigDecimal, null, null, UTILISATEUR_ID)))
                    .isInstanceOf(MouvementStockInvalideException.class);
        }

        @Test
        @DisplayName("quantité négative ou nulle → IllegalArgumentException")
        void creer_quantiteInvalide_leveException() {
            assertThatThrownBy(() -> MouvementStock.creer(new MouvementStock.CreationCommand(TypeMouvement.ENTREE_ACHAT, SensMouvement.ENTREE, null,
                    ENTREPOT_ID, null, LOT_ID, MEDICAMENT_ID, BigDecimal.ZERO, null, null, UTILISATEUR_ID)))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("utilisateur null → NullPointerException")
        void creer_utilisateurNull_leveException() {
            var bigDecimal = new BigDecimal("1");
            assertThatThrownBy(() -> MouvementStock.creer(new MouvementStock.CreationCommand(TypeMouvement.ENTREE_ACHAT, SensMouvement.ENTREE, null, ENTREPOT_ID, null, LOT_ID, MEDICAMENT_ID, bigDecimal, null, null, null)))
                    .isInstanceOf(NullPointerException.class);
        }
    }
}
