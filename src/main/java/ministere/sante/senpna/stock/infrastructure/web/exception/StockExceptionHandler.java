package ministere.sante.senpna.stock.infrastructure.web.exception;

import ministere.sante.senpna.shared.domain.exception.SenPnaException;
import ministere.sante.senpna.shared.infrastructure.web.response.RestResponse;
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
import ministere.sante.senpna.stock.infrastructure.web.controller.implement.LotsController;
import ministere.sante.senpna.stock.infrastructure.web.controller.implement.MouvementsStockController;
import ministere.sante.senpna.stock.infrastructure.web.controller.implement.StocksController;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

/**
 * Gestionnaire d'exceptions dédié à la feature {@code stock} (stocks,
 * lots, mouvements de stock).
 *
 * <h3>Exceptions gérées</h3>
 *
 * <pre>
 * StockIntrouvableException           → 404  ligne de stock introuvable
 * LotIntrouvableException             → 404  lot introuvable
 * MouvementStockIntrouvableException  → 404  mouvement de stock introuvable
 * NumeroLotDejaUtiliseException       → 409  numéro de lot déjà utilisé pour ce médicament
 * AucunEntrepotAffecteException       → 403  aucun entrepôt affecté à l'utilisateur
 * PorteeEntrepotInterditeException    → 403  entrepôt hors de la portée de l'utilisateur
 * LotHorsPorteeException              → 403  lot rattaché à un entrepôt hors portée
 * MouvementStockInvalideException     → 400  mouvement incohérent (quantité, sens, type)
 * StockInsuffisantException           → 422  quantité en stock insuffisante pour l'opération
 * ReservationInsuffisanteException    → 422  quantité réservée insuffisante pour l'opération
 * LotExpireException                  → 422  lot périmé, opération refusée
 * LotNonDisponibleException           → 422  lot non disponible (quarantaine, bloqué...)
 * </pre>
 */
@RestControllerAdvice(assignableTypes = {
                StocksController.class,
                LotsController.class,
                MouvementsStockController.class
})
public class StockExceptionHandler {

        private static final Logger log = LoggerFactory.getLogger(StockExceptionHandler.class);

        @ExceptionHandler(StockIntrouvableException.class)
        public ResponseEntity<Map<String, Object>> handleStockIntrouvable(StockIntrouvableException ex) {
                return build(HttpStatus.NOT_FOUND, ex);
        }

        @ExceptionHandler(LotIntrouvableException.class)
        public ResponseEntity<Map<String, Object>> handleLotIntrouvable(LotIntrouvableException ex) {
                return build(HttpStatus.NOT_FOUND, ex);
        }

        @ExceptionHandler(MouvementStockIntrouvableException.class)
        public ResponseEntity<Map<String, Object>> handleMouvementIntrouvable(
                        MouvementStockIntrouvableException ex) {
                return build(HttpStatus.NOT_FOUND, ex);
        }

        @ExceptionHandler(NumeroLotDejaUtiliseException.class)
        public ResponseEntity<Map<String, Object>> handleNumeroLotDejaUtilise(NumeroLotDejaUtiliseException ex) {
                return build(HttpStatus.CONFLICT, ex);
        }

        @ExceptionHandler(AucunEntrepotAffecteException.class)
        public ResponseEntity<Map<String, Object>> handleAucunEntrepotAffecte(AucunEntrepotAffecteException ex) {
                return build(HttpStatus.FORBIDDEN, ex);
        }

        @ExceptionHandler(PorteeEntrepotInterditeException.class)
        public ResponseEntity<Map<String, Object>> handlePorteeInterdite(PorteeEntrepotInterditeException ex) {
                return build(HttpStatus.FORBIDDEN, ex);
        }

        @ExceptionHandler(LotHorsPorteeException.class)
        public ResponseEntity<Map<String, Object>> handleLotHorsPortee(LotHorsPorteeException ex) {
                return build(HttpStatus.FORBIDDEN, ex);
        }

        @ExceptionHandler(MouvementStockInvalideException.class)
        public ResponseEntity<Map<String, Object>> handleMouvementInvalide(MouvementStockInvalideException ex) {
                return build(HttpStatus.BAD_REQUEST, ex);
        }

        @ExceptionHandler(StockInsuffisantException.class)
        public ResponseEntity<Map<String, Object>> handleStockInsuffisant(StockInsuffisantException ex) {
                return build(HttpStatus.UNPROCESSABLE_ENTITY, ex);
        }

        @ExceptionHandler(ReservationInsuffisanteException.class)
        public ResponseEntity<Map<String, Object>> handleReservationInsuffisante(
                        ReservationInsuffisanteException ex) {
                return build(HttpStatus.UNPROCESSABLE_ENTITY, ex);
        }

        @ExceptionHandler(LotExpireException.class)
        public ResponseEntity<Map<String, Object>> handleLotExpire(LotExpireException ex) {
                return build(HttpStatus.UNPROCESSABLE_ENTITY, ex);
        }

        @ExceptionHandler(LotNonDisponibleException.class)
        public ResponseEntity<Map<String, Object>> handleLotNonDisponible(LotNonDisponibleException ex) {
                return build(HttpStatus.UNPROCESSABLE_ENTITY, ex);
        }

        private ResponseEntity<Map<String, Object>> build(HttpStatus status, SenPnaException ex) {
                log.warn("[STOCK] {} — {}", ex.getType(), ex.getMessage());
                return ResponseEntity.status(status).body(RestResponse.error(status, ex.getMessage(), ex.getType()));
        }
}
