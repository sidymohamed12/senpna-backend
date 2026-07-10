package ministere.sante.senpna.medicament.infrastructure.web.exception;

import ministere.sante.senpna.medicament.domain.exception.conditionnement.ConditionnementIntrouvableException;
import ministere.sante.senpna.medicament.domain.exception.conditionnement.DerniereUniteBaseException;
import ministere.sante.senpna.medicament.domain.exception.conditionnement.NiveauConditionnementDejaUtiliseException;
import ministere.sante.senpna.medicament.domain.exception.conditionnement.NomConditionnementDejaUtiliseException;
import ministere.sante.senpna.medicament.domain.exception.conditionnement.UniteBaseDejaDefinieException;
import ministere.sante.senpna.medicament.domain.exception.famille.CodeFamilleDejaUtiliseException;
import ministere.sante.senpna.medicament.domain.exception.famille.FamilleInactiveException;
import ministere.sante.senpna.medicament.domain.exception.famille.FamilleIntrouvableException;
import ministere.sante.senpna.medicament.domain.exception.forme.CodeFormeDejaUtiliseException;
import ministere.sante.senpna.medicament.domain.exception.forme.FormeInactiveException;
import ministere.sante.senpna.medicament.domain.exception.forme.FormeIntrouvableException;
import ministere.sante.senpna.medicament.domain.exception.medicament.CodeMedicamentDejaUtiliseException;
import ministere.sante.senpna.medicament.domain.exception.medicament.MedicamentInactifException;
import ministere.sante.senpna.medicament.domain.exception.medicament.MedicamentIntrouvableException;
import ministere.sante.senpna.medicament.infrastructure.web.controller.implement.ConditionnementsController;
import ministere.sante.senpna.medicament.infrastructure.web.controller.implement.FamillesController;
import ministere.sante.senpna.medicament.infrastructure.web.controller.implement.FormesController;
import ministere.sante.senpna.medicament.infrastructure.web.controller.implement.MedicamentsController;
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
 * Gestionnaire d'exceptions dédié à la feature {@code medicament} (familles,
 * formes, conditionnements, médicaments).
 *
 * <h3>Exceptions gérées</h3>
 *
 * <pre>
 * FamilleIntrouvableException              → 404  famille introuvable
 * FormeIntrouvableException                → 404  forme introuvable
 * ConditionnementIntrouvableException      → 404  conditionnement introuvable
 * MedicamentIntrouvableException           → 404  médicament introuvable
 * CodeFamilleDejaUtiliseException          → 409  code famille déjà utilisé
 * CodeFormeDejaUtiliseException            → 409  code forme déjà utilisé
 * CodeMedicamentDejaUtiliseException       → 409  code médicament déjà utilisé
 * NomConditionnementDejaUtiliseException   → 409  nom de conditionnement déjà utilisé
 * NiveauConditionnementDejaUtiliseException → 409 niveau de conditionnement déjà utilisé pour ce médicament
 * FamilleInactiveException                 → 422  famille désactivée
 * FormeInactiveException                   → 422  forme désactivée
 * MedicamentInactifException               → 422  médicament désactivé
 * DerniereUniteBaseException               → 422  suppression du dernier conditionnement unité de base interdite
 * UniteBaseDejaDefinieException            → 422  unité de base déjà définie pour ce médicament
 * </pre>
 */
@RestControllerAdvice(assignableTypes = {
                FamillesController.class,
                FormesController.class,
                ConditionnementsController.class,
                MedicamentsController.class
})
public class MedicamentExceptionHandler {

        private static final Logger log = LoggerFactory.getLogger(MedicamentExceptionHandler.class);

        @ExceptionHandler(FamilleIntrouvableException.class)
        public ResponseEntity<Map<String, Object>> handleFamilleIntrouvable(FamilleIntrouvableException ex) {
                return build(HttpStatus.NOT_FOUND, ex);
        }

        @ExceptionHandler(FormeIntrouvableException.class)
        public ResponseEntity<Map<String, Object>> handleFormeIntrouvable(FormeIntrouvableException ex) {
                return build(HttpStatus.NOT_FOUND, ex);
        }

        @ExceptionHandler(ConditionnementIntrouvableException.class)
        public ResponseEntity<Map<String, Object>> handleConditionnementIntrouvable(
                        ConditionnementIntrouvableException ex) {
                return build(HttpStatus.NOT_FOUND, ex);
        }

        @ExceptionHandler(MedicamentIntrouvableException.class)
        public ResponseEntity<Map<String, Object>> handleMedicamentIntrouvable(MedicamentIntrouvableException ex) {
                return build(HttpStatus.NOT_FOUND, ex);
        }

        @ExceptionHandler(CodeFamilleDejaUtiliseException.class)
        public ResponseEntity<Map<String, Object>> handleCodeFamilleDejaUtilise(
                        CodeFamilleDejaUtiliseException ex) {
                return build(HttpStatus.CONFLICT, ex);
        }

        @ExceptionHandler(CodeFormeDejaUtiliseException.class)
        public ResponseEntity<Map<String, Object>> handleCodeFormeDejaUtilise(CodeFormeDejaUtiliseException ex) {
                return build(HttpStatus.CONFLICT, ex);
        }

        @ExceptionHandler(CodeMedicamentDejaUtiliseException.class)
        public ResponseEntity<Map<String, Object>> handleCodeMedicamentDejaUtilise(
                        CodeMedicamentDejaUtiliseException ex) {
                return build(HttpStatus.CONFLICT, ex);
        }

        @ExceptionHandler(NomConditionnementDejaUtiliseException.class)
        public ResponseEntity<Map<String, Object>> handleNomConditionnementDejaUtilise(
                        NomConditionnementDejaUtiliseException ex) {
                return build(HttpStatus.CONFLICT, ex);
        }

        @ExceptionHandler(NiveauConditionnementDejaUtiliseException.class)
        public ResponseEntity<Map<String, Object>> handleNiveauConditionnementDejaUtilise(
                        NiveauConditionnementDejaUtiliseException ex) {
                return build(HttpStatus.CONFLICT, ex);
        }

        @ExceptionHandler(FamilleInactiveException.class)
        public ResponseEntity<Map<String, Object>> handleFamilleInactive(FamilleInactiveException ex) {
                return build(HttpStatus.UNPROCESSABLE_ENTITY, ex);
        }

        @ExceptionHandler(FormeInactiveException.class)
        public ResponseEntity<Map<String, Object>> handleFormeInactive(FormeInactiveException ex) {
                return build(HttpStatus.UNPROCESSABLE_ENTITY, ex);
        }

        @ExceptionHandler(MedicamentInactifException.class)
        public ResponseEntity<Map<String, Object>> handleMedicamentInactif(MedicamentInactifException ex) {
                return build(HttpStatus.UNPROCESSABLE_ENTITY, ex);
        }

        @ExceptionHandler(DerniereUniteBaseException.class)
        public ResponseEntity<Map<String, Object>> handleDerniereUniteBase(DerniereUniteBaseException ex) {
                return build(HttpStatus.UNPROCESSABLE_ENTITY, ex);
        }

        @ExceptionHandler(UniteBaseDejaDefinieException.class)
        public ResponseEntity<Map<String, Object>> handleUniteBaseDejaDefinie(UniteBaseDejaDefinieException ex) {
                return build(HttpStatus.UNPROCESSABLE_ENTITY, ex);
        }

        private ResponseEntity<Map<String, Object>> build(HttpStatus status, SenPnaException ex) {
                log.warn("[MEDICAMENT] {} — {}", ex.getType(), ex.getMessage());
                return ResponseEntity.status(status).body(RestResponse.error(status, ex.getMessage(), ex.getType()));
        }
}
