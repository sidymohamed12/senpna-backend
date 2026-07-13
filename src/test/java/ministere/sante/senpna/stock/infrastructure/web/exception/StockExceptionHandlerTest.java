package ministere.sante.senpna.stock.infrastructure.web.exception;

import ministere.sante.senpna.stock.domain.exception.AucunEntrepotAffecteException;
import ministere.sante.senpna.stock.domain.exception.PorteeEntrepotInterditeException;
import ministere.sante.senpna.stock.domain.exception.ReservationInsuffisanteException;
import ministere.sante.senpna.stock.domain.exception.lot.LotExpireException;
import ministere.sante.senpna.stock.domain.exception.lot.LotHorsPorteeException;
import ministere.sante.senpna.stock.domain.exception.lot.LotIntrouvableException;
import ministere.sante.senpna.stock.domain.exception.lot.LotNonDisponibleException;
import ministere.sante.senpna.stock.domain.exception.lot.NumeroLotDejaUtiliseException;
import ministere.sante.senpna.stock.domain.exception.mouvement.MouvementStockIntrouvableException;
import ministere.sante.senpna.stock.domain.exception.mouvement.MouvementStockInvalideException;
import ministere.sante.senpna.stock.domain.exception.stock.StockInsuffisantException;
import ministere.sante.senpna.stock.domain.exception.stock.StockIntrouvableException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("StockExceptionHandler — mapping des exceptions du module stock")
class StockExceptionHandlerTest {

    StockExceptionHandler sut = new StockExceptionHandler();

    @Test
    @DisplayName("StockIntrouvableException → 404")
    void stockIntrouvable_404() {
        assertThat(sut.handleStockIntrouvable(new StockIntrouvableException()).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("LotIntrouvableException → 404")
    void lotIntrouvable_404() {
        assertThat(sut.handleLotIntrouvable(new LotIntrouvableException()).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("MouvementStockIntrouvableException → 404")
    void mouvementIntrouvable_404() {
        assertThat(sut.handleMouvementIntrouvable(new MouvementStockIntrouvableException()).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("NumeroLotDejaUtiliseException → 409")
    void numeroLotDejaUtilise_409() {
        assertThat(sut.handleNumeroLotDejaUtilise(new NumeroLotDejaUtiliseException("L1")).getStatusCode())
                .isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    @DisplayName("AucunEntrepotAffecteException → 403")
    void aucunEntrepotAffecte_403() {
        assertThat(sut.handleAucunEntrepotAffecte(new AucunEntrepotAffecteException()).getStatusCode())
                .isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("PorteeEntrepotInterditeException → 403")
    void porteeInterdite_403() {
        assertThat(sut.handlePorteeInterdite(new PorteeEntrepotInterditeException()).getStatusCode())
                .isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("LotHorsPorteeException → 403")
    void lotHorsPortee_403() {
        assertThat(sut.handleLotHorsPortee(new LotHorsPorteeException()).getStatusCode())
                .isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("MouvementStockInvalideException → 400")
    void mouvementInvalide_400() {
        assertThat(sut.handleMouvementInvalide(new MouvementStockInvalideException("msg")).getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("StockInsuffisantException → 422")
    void stockInsuffisant_422() {
        assertThat(sut.handleStockInsuffisant(
                new StockInsuffisantException(BigDecimal.ONE, BigDecimal.TEN)).getStatusCode())
                .isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @Test
    @DisplayName("ReservationInsuffisanteException → 422")
    void reservationInsuffisante_422() {
        assertThat(sut.handleReservationInsuffisante(
                new ReservationInsuffisanteException(BigDecimal.ONE, BigDecimal.TEN)).getStatusCode())
                .isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @Test
    @DisplayName("LotExpireException → 422")
    void lotExpire_422() {
        assertThat(sut.handleLotExpire(new LotExpireException("L1")).getStatusCode())
                .isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @Test
    @DisplayName("LotNonDisponibleException → 422")
    void lotNonDisponible_422() {
        assertThat(sut.handleLotNonDisponible(new LotNonDisponibleException("L1")).getStatusCode())
                .isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }
}
