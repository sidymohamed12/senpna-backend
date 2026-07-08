package ministere.sante.senpna.utilisateurs.infrastructure.web.exception;

import ministere.sante.senpna.shared.domain.exception.SenPnaException;
import ministere.sante.senpna.shared.infrastructure.web.response.RestResponse;
import ministere.sante.senpna.utilisateurs.domain.exception.ActeurNonAffecteException;
import ministere.sante.senpna.utilisateurs.domain.exception.AutoDesactivationInterditeException;
import ministere.sante.senpna.utilisateurs.domain.exception.ConflitTypeEntrepotException;
import ministere.sante.senpna.utilisateurs.domain.exception.CreationRoleReserveeException;
import ministere.sante.senpna.utilisateurs.domain.exception.DernierRoleException;
import ministere.sante.senpna.utilisateurs.domain.exception.EmailDejaUtiliseException;
import ministere.sante.senpna.utilisateurs.domain.exception.EntrepotInactifException;
import ministere.sante.senpna.utilisateurs.domain.exception.EntrepotIntrouvableException;
import ministere.sante.senpna.utilisateurs.domain.exception.EntrepotNonApplicableException;
import ministere.sante.senpna.utilisateurs.domain.exception.EntrepotRequisException;
import ministere.sante.senpna.utilisateurs.domain.exception.GestionUtilisateurInterditeException;
import ministere.sante.senpna.utilisateurs.domain.exception.RoleDejaAssigneException;
import ministere.sante.senpna.utilisateurs.domain.exception.RoleIntrouvableException;
import ministere.sante.senpna.utilisateurs.domain.exception.RoleNonAssigneException;
import ministere.sante.senpna.utilisateurs.domain.exception.TypeEntrepotIncompatibleException;
import ministere.sante.senpna.utilisateurs.infrastructure.web.controller.implement.UsersController;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

/**
 * Gestionnaire d'exceptions dédié à la feature {@code utilisateurs}
 * (comptes, rôles, affectations à un entrepôt).
 *
 * <h3>Exceptions gérées</h3>
 *
 * <pre>
 * RoleIntrouvableException                → 404  rôle introuvable
 * EntrepotIntrouvableException            → 404  entrepôt introuvable
 * RoleNonAssigneException                 → 404  rôle non assigné à cet utilisateur
 * EmailDejaUtiliseException               → 409  e-mail déjà utilisé par un autre compte
 * RoleDejaAssigneException                → 409  rôle déjà assigné à cet utilisateur
 * GestionUtilisateurInterditeException    → 403  gestion de cet utilisateur hors de portée (hiérarchie de rôles)
 * CreationRoleReserveeException           → 422  création de ce rôle réservée à un contexte spécifique
 * EntrepotInactifException                → 422  entrepôt désactivé
 * EntrepotNonApplicableException          → 400  entrepôt fourni pour un rôle qui n'en nécessite pas
 * EntrepotRequisException                 → 400  entrepôt obligatoire manquant pour ce rôle
 * ConflitTypeEntrepotException            → 400  type d'entrepôt incompatible avec le rôle
 * TypeEntrepotIncompatibleException       → 422  type d'entrepôt incompatible avec l'affectation existante
 * DernierRoleException                    → 422  retrait du dernier rôle d'un utilisateur interdit
 * AutoDesactivationInterditeException     → 422  un utilisateur ne peut pas désactiver son propre compte
 * ActeurNonAffecteException               → 422  acteur non affecté à un entrepôt requis pour l'opération
 * </pre>
 */
@RestControllerAdvice(assignableTypes = UsersController.class)
public class UtilisateursExceptionHandler {

        private static final Logger log = LoggerFactory.getLogger(UtilisateursExceptionHandler.class);

        @ExceptionHandler(RoleIntrouvableException.class)
        public ResponseEntity<Map<String, Object>> handleRoleIntrouvable(RoleIntrouvableException ex) {
                return build(HttpStatus.NOT_FOUND, ex);
        }

        @ExceptionHandler(EntrepotIntrouvableException.class)
        public ResponseEntity<Map<String, Object>> handleEntrepotIntrouvable(EntrepotIntrouvableException ex) {
                return build(HttpStatus.NOT_FOUND, ex);
        }

        @ExceptionHandler(RoleNonAssigneException.class)
        public ResponseEntity<Map<String, Object>> handleRoleNonAssigne(RoleNonAssigneException ex) {
                return build(HttpStatus.NOT_FOUND, ex);
        }

        @ExceptionHandler(EmailDejaUtiliseException.class)
        public ResponseEntity<Map<String, Object>> handleEmailDejaUtilise(EmailDejaUtiliseException ex) {
                return build(HttpStatus.CONFLICT, ex);
        }

        @ExceptionHandler(RoleDejaAssigneException.class)
        public ResponseEntity<Map<String, Object>> handleRoleDejaAssigne(RoleDejaAssigneException ex) {
                return build(HttpStatus.CONFLICT, ex);
        }

        @ExceptionHandler(GestionUtilisateurInterditeException.class)
        public ResponseEntity<Map<String, Object>> handleGestionInterdite(GestionUtilisateurInterditeException ex) {
                return build(HttpStatus.FORBIDDEN, ex);
        }

        @ExceptionHandler(CreationRoleReserveeException.class)
        public ResponseEntity<Map<String, Object>> handleCreationRoleReservee(CreationRoleReserveeException ex) {
                return build(HttpStatus.UNPROCESSABLE_ENTITY, ex);
        }

        @ExceptionHandler(EntrepotInactifException.class)
        public ResponseEntity<Map<String, Object>> handleEntrepotInactif(EntrepotInactifException ex) {
                return build(HttpStatus.UNPROCESSABLE_ENTITY, ex);
        }

        @ExceptionHandler(EntrepotNonApplicableException.class)
        public ResponseEntity<Map<String, Object>> handleEntrepotNonApplicable(EntrepotNonApplicableException ex) {
                return build(HttpStatus.BAD_REQUEST, ex);
        }

        @ExceptionHandler(EntrepotRequisException.class)
        public ResponseEntity<Map<String, Object>> handleEntrepotRequis(EntrepotRequisException ex) {
                return build(HttpStatus.BAD_REQUEST, ex);
        }

        @ExceptionHandler(ConflitTypeEntrepotException.class)
        public ResponseEntity<Map<String, Object>> handleConflitTypeEntrepot(ConflitTypeEntrepotException ex) {
                return build(HttpStatus.BAD_REQUEST, ex);
        }

        @ExceptionHandler(TypeEntrepotIncompatibleException.class)
        public ResponseEntity<Map<String, Object>> handleTypeEntrepotIncompatible(
                        TypeEntrepotIncompatibleException ex) {
                return build(HttpStatus.UNPROCESSABLE_ENTITY, ex);
        }

        @ExceptionHandler(DernierRoleException.class)
        public ResponseEntity<Map<String, Object>> handleDernierRole(DernierRoleException ex) {
                return build(HttpStatus.UNPROCESSABLE_ENTITY, ex);
        }

        @ExceptionHandler(AutoDesactivationInterditeException.class)
        public ResponseEntity<Map<String, Object>> handleAutoDesactivationInterdite(
                        AutoDesactivationInterditeException ex) {
                return build(HttpStatus.UNPROCESSABLE_ENTITY, ex);
        }

        @ExceptionHandler(ActeurNonAffecteException.class)
        public ResponseEntity<Map<String, Object>> handleActeurNonAffecte(ActeurNonAffecteException ex) {
                return build(HttpStatus.UNPROCESSABLE_ENTITY, ex);
        }

        private ResponseEntity<Map<String, Object>> build(HttpStatus status, SenPnaException ex) {
                log.warn("[UTILISATEURS] {} — {}", ex.getType(), ex.getMessage());
                return ResponseEntity.status(status).body(RestResponse.error(status, ex.getMessage(), ex.getType()));
        }
}
