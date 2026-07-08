package ministere.sante.senpna.catalogue.infrastructure.web.exception;

import ministere.sante.senpna.catalogue.domain.exception.AucunePraPourRegionException;
import ministere.sante.senpna.catalogue.domain.exception.CatalogueAccesRefuseException;
import ministere.sante.senpna.catalogue.domain.exception.PnaCentraleIntrouvableException;
import ministere.sante.senpna.catalogue.infrastructure.web.controller.impl.CatalogueController;
import ministere.sante.senpna.shared.domain.exception.SenPnaException;
import ministere.sante.senpna.shared.infrastructure.web.response.RestResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

/**
 * Gestionnaire d'exceptions dédié à la feature {@code catalogue}.
 *
 * <h3>Exceptions gérées</h3>
 *
 * <pre>
 * PnaCentraleIntrouvableException → 404  PNA centrale introuvable
 * CatalogueAccesRefuseException   → 403  accès au catalogue refusé (portée région/entrepôt)
 * AucunePraPourRegionException    → 404  aucune PRA rattachée à la région demandée
 * </pre>
 */
@RestControllerAdvice(assignableTypes = CatalogueController.class)
public class CatalogueExceptionHandler {

        private static final Logger log = LoggerFactory.getLogger(CatalogueExceptionHandler.class);

        @ExceptionHandler(PnaCentraleIntrouvableException.class)
        public ResponseEntity<Map<String, Object>> handlePnaCentraleIntrouvable(PnaCentraleIntrouvableException ex) {
                return build(HttpStatus.NOT_FOUND, ex);
        }

        @ExceptionHandler(CatalogueAccesRefuseException.class)
        public ResponseEntity<Map<String, Object>> handleAccesRefuse(CatalogueAccesRefuseException ex) {
                return build(HttpStatus.FORBIDDEN, ex);
        }

        @ExceptionHandler(AucunePraPourRegionException.class)
        public ResponseEntity<Map<String, Object>> handleAucunePraPourRegion(AucunePraPourRegionException ex) {
                return build(HttpStatus.NOT_FOUND, ex);
        }

        private ResponseEntity<Map<String, Object>> build(HttpStatus status, SenPnaException ex) {
                log.warn("[CATALOGUE] {} — {}", ex.getType(), ex.getMessage());
                return ResponseEntity.status(status).body(RestResponse.error(status, ex.getMessage(), ex.getType()));
        }
}
