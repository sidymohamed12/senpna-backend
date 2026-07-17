package ministere.sante.senpna.stock.domain.model;

import ministere.sante.senpna.fournisseur.domain.valueobject.FournisseurId;
import ministere.sante.senpna.medicament.domain.valueobject.MedicamentId;
import ministere.sante.senpna.stock.domain.exception.lot.LotExpireException;
import ministere.sante.senpna.stock.domain.valueobject.StatutLot;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Lot — agrégat de domaine")
class LotTest {

    private static final MedicamentId MEDICAMENT_ID = MedicamentId.generate();
    private static final FournisseurId FOURNISSEUR_ID = FournisseurId.generate();

    private static Lot lotValide(LocalDate dateExpiration) {
        return Lot.creer(new Lot.CreationCommand("LOT-A001", MEDICAMENT_ID, FOURNISSEUR_ID,
                LocalDate.now().minusMonths(1), dateExpiration,
                new BigDecimal("100"), new BigDecimal("150")));
    }

    @Nested
    @DisplayName("creer()")
    class Creer {

        @Test
        @DisplayName("crée un lot ACTIF avec les informations fournies")
        void creer_succes() {
            Lot lot = lotValide(LocalDate.now().plusYears(1));

            assertThat(lot.getStatut()).isEqualTo(StatutLot.ACTIF);
            assertThat(lot.getNumeroLot()).isEqualTo("LOT-A001");
            assertThat(lot.getMedicamentId()).isEqualTo(MEDICAMENT_ID);
            assertThat(lot.getFournisseurId()).isEqualTo(FOURNISSEUR_ID);
        }

        @Test
        @DisplayName("numéro de lot vide → IllegalArgumentException")
        void creer_numeroLotVide_leveException() {
            var now = LocalDate.now().plusYears(1);
            var creationCommand = new Lot.CreationCommand("  ", MEDICAMENT_ID, FOURNISSEUR_ID, null, now, null, null);
            assertThatThrownBy(() -> Lot.creer(creationCommand))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("date d'expiration null → NullPointerException")
        void creer_dateExpirationNull_leveException() {
            var creationCommand = new Lot.CreationCommand("LOT-A001", MEDICAMENT_ID, FOURNISSEUR_ID, null, null, null,
                    null);

            assertThatThrownBy(() -> Lot.creer(creationCommand))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("date de fabrication postérieure à la date d'expiration → IllegalArgumentException")
        void creer_fabricationApresExpiration_leveException() {
            var now = LocalDate.now().plusDays(10);
            var now2 = LocalDate.now();
            var creationCommand = new Lot.CreationCommand("LOT-A001", MEDICAMENT_ID, FOURNISSEUR_ID, now, now2, null,
                    null);

            assertThatThrownBy(() -> Lot.creer(creationCommand))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("prix négatif → IllegalArgumentException")
        void creer_prixNegatif_leveException() {
            var now = LocalDate.now().plusYears(1);
            var bigDecimal = new BigDecimal("-1");
            var creationCommand = new Lot.CreationCommand("LOT-A001", MEDICAMENT_ID, FOURNISSEUR_ID, null, now,
                    bigDecimal, null);

            assertThatThrownBy(() -> Lot.creer(creationCommand))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("medicamentId null → NullPointerException")
        void creer_medicamentIdNull_leveException() {
            LocalDate datePeremption = LocalDate.now().plusYears(1);
            var creationCommand = new Lot.CreationCommand("LOT-A001", null, FOURNISSEUR_ID, null, datePeremption, null,
                    null);

            assertThatThrownBy(
                    () -> Lot.creer(creationCommand))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("peutEtreReserveOuExpedie() / estExpire()")
    class Disponibilite {

        @Test
        @DisplayName("lot ACTIF non expiré → disponible")
        void lotActifNonExpire_disponible() {
            Lot lot = lotValide(LocalDate.now().plusMonths(6));

            assertThat(lot.estExpire()).isFalse();
            assertThat(lot.peutEtreReserveOuExpedie()).isTrue();
        }

        @Test
        @DisplayName("lot dont la date d'expiration est passée → expiré, non disponible")
        void lotExpire_nonDisponible() {
            Lot lot = lotValide(LocalDate.now().minusDays(1));

            assertThat(lot.estExpire()).isTrue();
            assertThat(lot.peutEtreReserveOuExpedie()).isFalse();
        }

        @Test
        @DisplayName("lot bloqué → non disponible même si non expiré")
        void lotBloque_nonDisponible() {
            Lot lot = lotValide(LocalDate.now().plusMonths(6));
            lot.bloquer();

            assertThat(lot.peutEtreReserveOuExpedie()).isFalse();
        }
    }

    @Nested
    @DisplayName("bloquer() / debloquer()")
    class BloquerDebloquer {

        @Test
        @DisplayName("bloquer un lot ACTIF le passe en BLOQUE")
        void bloquer_succes() {
            Lot lot = lotValide(LocalDate.now().plusMonths(6));

            lot.bloquer();

            assertThat(lot.getStatut()).isEqualTo(StatutLot.BLOQUE);
        }

        @Test
        @DisplayName("débloquer un lot BLOQUE le repasse en ACTIF")
        void debloquer_succes() {
            Lot lot = lotValide(LocalDate.now().plusMonths(6));
            lot.bloquer();

            lot.debloquer();

            assertThat(lot.getStatut()).isEqualTo(StatutLot.ACTIF);
        }

        @Test
        @DisplayName("bloquer un lot déjà EXPIRE → LotExpireException")
        void bloquer_lotExpire_leveException() {
            Lot lot = lotValide(LocalDate.now().minusDays(1));
            lot.marquerExpire();

            assertThatThrownBy(lot::bloquer).isInstanceOf(LotExpireException.class);
        }

        @Test
        @DisplayName("débloquer un lot EXPIRE → LotExpireException")
        void debloquer_lotExpire_leveException() {
            Lot lot = lotValide(LocalDate.now().minusDays(1));
            lot.marquerExpire();

            assertThatThrownBy(lot::debloquer).isInstanceOf(LotExpireException.class);
        }
    }

    @Nested
    @DisplayName("marquerExpire()")
    class MarquerExpire {

        @Test
        @DisplayName("positionne le statut à EXPIRE")
        void marquerExpire_succes() {
            Lot lot = lotValide(LocalDate.now().minusDays(1));

            lot.marquerExpire();

            assertThat(lot.getStatut()).isEqualTo(StatutLot.EXPIRE);
        }

        @Test
        @DisplayName("idempotent — appeler deux fois ne lève pas d'exception")
        void marquerExpire_idempotent() {
            Lot lot = lotValide(LocalDate.now().minusDays(1));

            lot.marquerExpire();
            lot.marquerExpire();

            assertThat(lot.getStatut()).isEqualTo(StatutLot.EXPIRE);
        }
    }
}
