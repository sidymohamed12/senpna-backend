package ministere.sante.senpna.commandeachat.infrastructure.web.exception;

import ministere.sante.senpna.commandeachat.domain.exception.AccesCommandeAchatRefuseException;
import ministere.sante.senpna.commandeachat.domain.exception.AccesFactureRefuseException;
import ministere.sante.senpna.commandeachat.domain.exception.CommandeAchatIntrouvableException;
import ministere.sante.senpna.commandeachat.domain.exception.FactureIntrouvableException;
import ministere.sante.senpna.commandeachat.domain.exception.LigneCommandeAchatIntrouvableException;
import ministere.sante.senpna.commandeachat.domain.exception.ReferenceCommandeAchatDejaUtiliseeException;
import ministere.sante.senpna.commandeachat.domain.exception.TransitionStatutCommandeAchatInvalideException;
import ministere.sante.senpna.commandeachat.domain.exception.TransitionStatutFactureInvalideException;
import ministere.sante.senpna.commandeachat.infrastructure.web.controller.implement.CommandesAchatController;
import ministere.sante.senpna.commandeachat.infrastructure.web.controller.implement.EspaceFournisseurCommandesAchatController;
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
 * Gestionnaire d'exceptions dédié à la feature {@code commandeachat}
 * (commandes d'achat PNA et espace fournisseur, factures).
 *
 * <h3>Exceptions gérées</h3>
 *
 * <pre>
 * CommandeAchatIntrouvableException            → 404  commande introuvable
 * LigneCommandeAchatIntrouvableException       → 404  ligne de commande introuvable
 * FactureIntrouvableException                  → 404  facture introuvable
 * ReferenceCommandeAchatDejaUtiliseeException   → 409  référence déjà utilisée
 * AccesCommandeAchatRefuseException             → 403  commande appartenant à un autre fournisseur
 * AccesFactureRefuseException                   → 403  facture appartenant à un autre fournisseur
 * TransitionStatutCommandeAchatInvalideException → 422 transition de statut de commande invalide
 * TransitionStatutFactureInvalideException      → 422  transition de statut de facture invalide
 * </pre>
 */
@RestControllerAdvice(assignableTypes = {
        CommandesAchatController.class,
        EspaceFournisseurCommandesAchatController.class })
public class CommandeAchatExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(CommandeAchatExceptionHandler.class);

    @ExceptionHandler(CommandeAchatIntrouvableException.class)
    public ResponseEntity<Map<String, Object>> handleCommandeIntrouvable(CommandeAchatIntrouvableException ex) {
        return build(HttpStatus.NOT_FOUND, ex);
    }

    @ExceptionHandler(LigneCommandeAchatIntrouvableException.class)
    public ResponseEntity<Map<String, Object>> handleLigneIntrouvable(LigneCommandeAchatIntrouvableException ex) {
        return build(HttpStatus.NOT_FOUND, ex);
    }

    @ExceptionHandler(FactureIntrouvableException.class)
    public ResponseEntity<Map<String, Object>> handleFactureIntrouvable(FactureIntrouvableException ex) {
        return build(HttpStatus.NOT_FOUND, ex);
    }

    @ExceptionHandler(ReferenceCommandeAchatDejaUtiliseeException.class)
    public ResponseEntity<Map<String, Object>> handleReferenceDejaUtilisee(
            ReferenceCommandeAchatDejaUtiliseeException ex) {
        return build(HttpStatus.CONFLICT, ex);
    }

    @ExceptionHandler(AccesCommandeAchatRefuseException.class)
    public ResponseEntity<Map<String, Object>> handleAccesCommandeRefuse(AccesCommandeAchatRefuseException ex) {
        return build(HttpStatus.FORBIDDEN, ex);
    }

    @ExceptionHandler(AccesFactureRefuseException.class)
    public ResponseEntity<Map<String, Object>> handleAccesFactureRefuse(AccesFactureRefuseException ex) {
        return build(HttpStatus.FORBIDDEN, ex);
    }

    @ExceptionHandler(TransitionStatutCommandeAchatInvalideException.class)
    public ResponseEntity<Map<String, Object>> handleTransitionCommandeInvalide(
            TransitionStatutCommandeAchatInvalideException ex) {
        return build(HttpStatus.UNPROCESSABLE_ENTITY, ex);
    }

    @ExceptionHandler(TransitionStatutFactureInvalideException.class)
    public ResponseEntity<Map<String, Object>> handleTransitionFactureInvalide(
            TransitionStatutFactureInvalideException ex) {
        return build(HttpStatus.UNPROCESSABLE_ENTITY, ex);
    }

    private ResponseEntity<Map<String, Object>> build(HttpStatus status, SenPnaException ex) {
        log.warn("[COMMANDE_ACHAT] {} — {}", ex.getType(), ex.getMessage());
        return ResponseEntity.status(status).body(RestResponse.error(status, ex.getMessage(), ex.getType()));
    }
}
