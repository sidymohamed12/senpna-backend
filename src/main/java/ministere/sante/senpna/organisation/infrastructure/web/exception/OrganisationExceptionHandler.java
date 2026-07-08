package ministere.sante.senpna.organisation.infrastructure.web.exception;

import ministere.sante.senpna.organisation.domain.exception.AccesRegionRefuseException;
import ministere.sante.senpna.organisation.domain.exception.CodeEntrepotDejaUtiliseException;
import ministere.sante.senpna.organisation.domain.exception.CodeRegionDejaUtiliseException;
import ministere.sante.senpna.organisation.domain.exception.CodeStructureSanitaireDejaUtiliseException;
import ministere.sante.senpna.organisation.domain.exception.DemandeAdhesionDejaTraiteeException;
import ministere.sante.senpna.organisation.domain.exception.EntrepotInactifException;
import ministere.sante.senpna.organisation.domain.exception.EntrepotIntrouvableException;
import ministere.sante.senpna.organisation.domain.exception.RegionInactiveException;
import ministere.sante.senpna.organisation.domain.exception.RegionIntrouvableException;
import ministere.sante.senpna.organisation.domain.exception.StructureSanitaireIntrouvableException;
import ministere.sante.senpna.organisation.domain.exception.StructureSanitaireNonValideeException;
import ministere.sante.senpna.organisation.domain.exception.TypeEntrepotInvalideException;
import ministere.sante.senpna.organisation.infrastructure.web.controller.implement.AffectationsController;
import ministere.sante.senpna.organisation.infrastructure.web.controller.implement.EntrepotsController;
import ministere.sante.senpna.organisation.infrastructure.web.controller.implement.PrasController;
import ministere.sante.senpna.organisation.infrastructure.web.controller.implement.RegionsController;
import ministere.sante.senpna.organisation.infrastructure.web.controller.implement.StructuresSanitairesController;
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
 * Gestionnaire d'exceptions dédié à la feature {@code organisation}
 * (régions, structures sanitaires, entrepôts, PRA, affectations).
 *
 * <h3>Exceptions gérées</h3>
 *
 * <pre>
 * RegionIntrouvableException                  → 404  région introuvable
 * StructureSanitaireIntrouvableException      → 404  structure sanitaire introuvable
 * EntrepotIntrouvableException                → 404  entrepôt introuvable
 * CodeRegionDejaUtiliseException              → 409  code région déjà utilisé
 * CodeStructureSanitaireDejaUtiliseException  → 409  code structure sanitaire déjà utilisé
 * CodeEntrepotDejaUtiliseException            → 409  code entrepôt déjà utilisé
 * AccesRegionRefuseException                  → 403  accès à la région refusé (portée géographique)
 * RegionInactiveException                     → 422  région désactivée
 * EntrepotInactifException                    → 422  entrepôt désactivé
 * StructureSanitaireNonValideeException       → 422  structure sanitaire pas encore validée (adhésion en attente)
 * DemandeAdhesionDejaTraiteeException         → 422  demande d'adhésion déjà traitée
 * TypeEntrepotInvalideException               → 422  type d'entrepôt incompatible avec le contexte
 * </pre>
 */
@RestControllerAdvice(assignableTypes = {
                RegionsController.class,
                StructuresSanitairesController.class,
                EntrepotsController.class,
                PrasController.class,
                AffectationsController.class
})
public class OrganisationExceptionHandler {

        private static final Logger log = LoggerFactory.getLogger(OrganisationExceptionHandler.class);

        @ExceptionHandler(RegionIntrouvableException.class)
        public ResponseEntity<Map<String, Object>> handleRegionIntrouvable(RegionIntrouvableException ex) {
                return build(HttpStatus.NOT_FOUND, ex);
        }

        @ExceptionHandler(StructureSanitaireIntrouvableException.class)
        public ResponseEntity<Map<String, Object>> handleStructureIntrouvable(
                        StructureSanitaireIntrouvableException ex) {
                return build(HttpStatus.NOT_FOUND, ex);
        }

        @ExceptionHandler(EntrepotIntrouvableException.class)
        public ResponseEntity<Map<String, Object>> handleEntrepotIntrouvable(EntrepotIntrouvableException ex) {
                return build(HttpStatus.NOT_FOUND, ex);
        }

        @ExceptionHandler(CodeRegionDejaUtiliseException.class)
        public ResponseEntity<Map<String, Object>> handleCodeRegionDejaUtilise(CodeRegionDejaUtiliseException ex) {
                return build(HttpStatus.CONFLICT, ex);
        }

        @ExceptionHandler(CodeStructureSanitaireDejaUtiliseException.class)
        public ResponseEntity<Map<String, Object>> handleCodeStructureDejaUtilise(
                        CodeStructureSanitaireDejaUtiliseException ex) {
                return build(HttpStatus.CONFLICT, ex);
        }

        @ExceptionHandler(CodeEntrepotDejaUtiliseException.class)
        public ResponseEntity<Map<String, Object>> handleCodeEntrepotDejaUtilise(
                        CodeEntrepotDejaUtiliseException ex) {
                return build(HttpStatus.CONFLICT, ex);
        }

        @ExceptionHandler(AccesRegionRefuseException.class)
        public ResponseEntity<Map<String, Object>> handleAccesRegionRefuse(AccesRegionRefuseException ex) {
                return build(HttpStatus.FORBIDDEN, ex);
        }

        @ExceptionHandler(RegionInactiveException.class)
        public ResponseEntity<Map<String, Object>> handleRegionInactive(RegionInactiveException ex) {
                return build(HttpStatus.UNPROCESSABLE_ENTITY, ex);
        }

        @ExceptionHandler(EntrepotInactifException.class)
        public ResponseEntity<Map<String, Object>> handleEntrepotInactif(EntrepotInactifException ex) {
                return build(HttpStatus.UNPROCESSABLE_ENTITY, ex);
        }

        @ExceptionHandler(StructureSanitaireNonValideeException.class)
        public ResponseEntity<Map<String, Object>> handleStructureNonValidee(
                        StructureSanitaireNonValideeException ex) {
                return build(HttpStatus.UNPROCESSABLE_ENTITY, ex);
        }

        @ExceptionHandler(DemandeAdhesionDejaTraiteeException.class)
        public ResponseEntity<Map<String, Object>> handleDemandeAdhesionDejaTraitee(
                        DemandeAdhesionDejaTraiteeException ex) {
                return build(HttpStatus.UNPROCESSABLE_ENTITY, ex);
        }

        @ExceptionHandler(TypeEntrepotInvalideException.class)
        public ResponseEntity<Map<String, Object>> handleTypeEntrepotInvalide(TypeEntrepotInvalideException ex) {
                return build(HttpStatus.UNPROCESSABLE_ENTITY, ex);
        }

        private ResponseEntity<Map<String, Object>> build(HttpStatus status, SenPnaException ex) {
                log.warn("[ORGANISATION] {} — {}", ex.getType(), ex.getMessage());
                return ResponseEntity.status(status).body(RestResponse.error(status, ex.getMessage(), ex.getType()));
        }
}
