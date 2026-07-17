package ministere.sante.senpna.stock.domain.model;

import ministere.sante.senpna.medicament.domain.valueobject.MedicamentId;
import ministere.sante.senpna.organisation.domain.valueobject.EntrepotId;
import ministere.sante.senpna.stock.domain.exception.ReservationInsuffisanteException;
import ministere.sante.senpna.stock.domain.exception.stock.StockInsuffisantException;
import ministere.sante.senpna.stock.domain.valueobject.LotId;
import ministere.sante.senpna.stock.domain.valueobject.StockId;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Stock — agrégat de domaine")
class StockTest {

    private static final EntrepotId ENTREPOT_ID = EntrepotId.generate();
    private static final LotId LOT_ID = LotId.generate();
    private static final MedicamentId MEDICAMENT_ID = MedicamentId.generate();

    private static Stock stockOuvert() {
        return Stock.ouvrir(new Stock.OuvertureCommand(ENTREPOT_ID, LOT_ID, MEDICAMENT_ID, new BigDecimal("10")));
    }

    @Nested
    @DisplayName("ouvrir()")
    class Ouvrir {

        @Test
        @DisplayName("ouvre une ligne de stock à zéro")
        void ouvrir_succes() {
            Stock stock = stockOuvert();

            assertThat(stock.getQuantiteDisponible()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(stock.getQuantiteReservee()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(stock.getQuantiteEnCommande()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(stock.estEnRupture()).isTrue();
        }
    }

    @Nested
    @DisplayName("entrer()")
    class Entrer {

        @Test
        @DisplayName("augmente la quantité disponible")
        void entrer_succes() {
            Stock stock = stockOuvert();

            stock.entrer(new BigDecimal("100"));

            assertThat(stock.getQuantiteDisponible()).isEqualByComparingTo(new BigDecimal("100"));
            assertThat(stock.getQuantiteDisponibleALaVente()).isEqualByComparingTo(new BigDecimal("100"));
        }

        @Test
        @DisplayName("quantité négative ou nulle → IllegalArgumentException")
        void entrer_quantiteInvalide_leveException() {
            Stock stock = stockOuvert();

            assertThatThrownBy(() -> stock.entrer(BigDecimal.ZERO)).isInstanceOf(IllegalArgumentException.class);
            var bigDecimal = new BigDecimal("-5");
            assertThatThrownBy(() -> stock.entrer(bigDecimal))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("reserver()")
    class Reserver {

        @Test
        @DisplayName("augmente la quantité réservée sans dépasser la quantité disponible")
        void reserver_succes() {
            Stock stock = stockOuvert();
            stock.entrer(new BigDecimal("100"));

            stock.reserver(new BigDecimal("40"));

            assertThat(stock.getQuantiteReservee()).isEqualByComparingTo(new BigDecimal("40"));
            assertThat(stock.getQuantiteDisponibleALaVente()).isEqualByComparingTo(new BigDecimal("60"));
        }

        @Test
        @DisplayName("quantité supérieure à la quantité disponible à la vente → StockInsuffisantException")
        void reserver_quantiteExcessive_leveException() {
            Stock stock = stockOuvert();
            stock.entrer(new BigDecimal("50"));

            var bigDecimal = new BigDecimal("51");
            assertThatThrownBy(() -> stock.reserver(bigDecimal))
                    .isInstanceOf(StockInsuffisantException.class);
        }

        @Test
        @DisplayName("deux réservations successives cumulent la quantité réservée")
        void reserver_cumule() {
            Stock stock = stockOuvert();
            stock.entrer(new BigDecimal("100"));

            stock.reserver(new BigDecimal("30"));
            stock.reserver(new BigDecimal("20"));

            assertThat(stock.getQuantiteReservee()).isEqualByComparingTo(new BigDecimal("50"));
        }
    }

    @Nested
    @DisplayName("libererReservation()")
    class LibererReservation {

        @Test
        @DisplayName("diminue la quantité réservée")
        void liberer_succes() {
            Stock stock = stockOuvert();
            stock.entrer(new BigDecimal("100"));
            stock.reserver(new BigDecimal("40"));

            stock.libererReservation(new BigDecimal("15"));

            assertThat(stock.getQuantiteReservee()).isEqualByComparingTo(new BigDecimal("25"));
            assertThat(stock.getQuantiteDisponibleALaVente()).isEqualByComparingTo(new BigDecimal("75"));
        }

        @Test
        @DisplayName("libérer plus que la quantité réservée → ReservationInsuffisanteException")
        void liberer_quantiteExcessive_leveException() {
            Stock stock = stockOuvert();
            stock.entrer(new BigDecimal("100"));
            stock.reserver(new BigDecimal("10"));

            var bigDecimal = new BigDecimal("11");
            assertThatThrownBy(() -> stock.libererReservation(bigDecimal))
                    .isInstanceOf(ReservationInsuffisanteException.class);
        }
    }

    @Nested
    @DisplayName("sortir()")
    class Sortir {

        @Test
        @DisplayName("diminue la quantité disponible sans toucher la réservation")
        void sortir_succes() {
            Stock stock = stockOuvert();
            stock.entrer(new BigDecimal("100"));
            stock.reserver(new BigDecimal("20"));

            stock.sortir(new BigDecimal("30"));

            assertThat(stock.getQuantiteDisponible()).isEqualByComparingTo(new BigDecimal("70"));
            assertThat(stock.getQuantiteReservee()).isEqualByComparingTo(new BigDecimal("20"));
        }

        @Test
        @DisplayName("ne peut pas descendre sous la quantité déjà réservée")
        void sortir_protegeQuantiteReservee() {
            Stock stock = stockOuvert();
            stock.entrer(new BigDecimal("100"));
            stock.reserver(new BigDecimal("90"));

            var bigDecimal = new BigDecimal("11");
            assertThatThrownBy(() -> stock.sortir(bigDecimal))
                    .isInstanceOf(StockInsuffisantException.class);
        }
    }

    @Nested
    @DisplayName("sortirDepuisReservation()")
    class SortirDepuisReservation {

        @Test
        @DisplayName("diminue à la fois la quantité disponible et la quantité réservée")
        void sortirDepuisReservation_succes() {
            Stock stock = stockOuvert();
            stock.entrer(new BigDecimal("100"));
            stock.reserver(new BigDecimal("40"));

            stock.sortirDepuisReservation(new BigDecimal("40"));

            assertThat(stock.getQuantiteDisponible()).isEqualByComparingTo(new BigDecimal("60"));
            assertThat(stock.getQuantiteReservee()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("quantité supérieure à la réservation → ReservationInsuffisanteException")
        void sortirDepuisReservation_quantiteExcessive_leveException() {
            Stock stock = stockOuvert();
            stock.entrer(new BigDecimal("100"));
            stock.reserver(new BigDecimal("10"));

            var bigDecimal = new BigDecimal("11");
            assertThatThrownBy(() -> stock.sortirDepuisReservation(bigDecimal))
                    .isInstanceOf(ReservationInsuffisanteException.class);
        }
    }

    @Nested
    @DisplayName("estEnRupture() / seuilAtteint()")
    class Alertes {

        @Test
        @DisplayName("rupture quand la quantité disponible à la vente est nulle")
        void estEnRupture_quandDisponibleNul() {
            Stock stock = stockOuvert();

            assertThat(stock.estEnRupture()).isTrue();

            stock.entrer(new BigDecimal("10"));
            assertThat(stock.estEnRupture()).isFalse();
        }

        @Test
        @DisplayName("seuil atteint quand la quantité disponible à la vente descend au niveau du seuil")
        void seuilAtteint_succes() {
            Stock stock = stockOuvert(); // seuil = 10
            stock.entrer(new BigDecimal("15"));

            assertThat(stock.seuilAtteint()).isFalse();

            stock.reserver(new BigDecimal("6")); // disponible à la vente = 9 <= seuil 10
            assertThat(stock.seuilAtteint()).isTrue();
        }

        @Test
        @DisplayName("seuil non défini → jamais considéré comme atteint")
        void seuilAtteint_seuilNonDefini_toujoursFaux() {
            Stock stock = Stock.ouvrir(new Stock.OuvertureCommand(ENTREPOT_ID, LOT_ID, MEDICAMENT_ID, null));

            assertThat(stock.seuilAtteint()).isFalse();
        }
    }

    @Nested
    @DisplayName("invariant réservée <= disponible")
    class InvariantReserveeSousDisponible {

        @Test
        @DisplayName("reconstruction avec réservée > disponible → IllegalArgumentException")
        void reconstruct_reserveeSuperieureADisponible_leveException() {
            var stockBuilder = Stock.builder()
                    .id(StockId.generate())
                    .entrepotId(ENTREPOT_ID)
                    .lotId(LOT_ID)
                    .medicamentId(MEDICAMENT_ID)
                    .quantiteDisponible(new BigDecimal("10"))
                    .quantiteReservee(new BigDecimal("20"))
                    .quantiteEnCommande(BigDecimal.ZERO)
                    .seuilAlerte(null)
                    .createdAt(java.time.Instant.now())
                    .updatedAt(java.time.Instant.now());

            assertThatThrownBy(stockBuilder::build)
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }
}
