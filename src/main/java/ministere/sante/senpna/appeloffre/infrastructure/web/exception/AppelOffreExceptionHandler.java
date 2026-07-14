package ministere.sante.senpna.appeloffre.infrastructure.web.exception;

import ministere.sante.senpna.appeloffre.domain.exception.AccesOffreRefuseException;
import ministere.sante.senpna.appeloffre.domain.exception.AppelOffreIntrouvableException;
import ministere.sante.senpna.appeloffre.domain.exception.AppelOffreNonPublieException;
import ministere.sante.senpna.appeloffre.domain.exception.DateClotureDepasseeException;
import ministere.sante.senpna.appeloffre.domain.exception.OffreDejaSoumiseException;
import ministere.sante.senpna.appeloffre.domain.exception.OffreFournisseurIntrouvableException;
import ministere.sante.senpna.appeloffre.domain.exception.ReferenceAppelOffreDejaUtiliseeException;
import ministere.sante.senpna.appeloffre.domain.exception.TransitionStatutAppelOffreInvalideException;
import ministere.sante.senpna.appeloffre.domain.exception.TransitionStatutOffreInvalideException;
import ministere.sante.senpna.appeloffre.infrastructure.web.controller.implement.AppelOffresController;
import ministere.sante.senpna.appeloffre.infrastructure.web.controller.implement.EspaceFournisseurAppelOffresController;
import ministere.sante.senpna.appeloffre.infrastructure.web.controller.implement.EspaceFournisseurOffresController;
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
 * Gestionnaire d'exceptions dédié à la feature {@code appeloffre}
 * (appels d'offres PNA et espace fournisseur).
 *
 * <h3>Exceptions gérées</h3>
 *
 * <pre>
 * AppelOffreIntrouvableException              → 404  appel d'offres introuvable (ou non visible côté fournisseur)
 * OffreFournisseurIntrouvableException        → 404  offre introuvable
 * ReferenceAppelOffreDejaUtiliseeException    → 409  référence déjà utilisée
 * AccesOffreRefuseException                   → 403  offre appartenant à un autre fournisseur
 * AppelOffreNonPublieException                → 422  AO non ouvert à la soumission
 * DateClotureDepasseeException                → 422  date de clôture dépassée
 * OffreDejaSoumiseException                   → 409  offre déjà soumise par ce fournisseur
 * TransitionStatutAppelOffreInvalideException → 422  transition de statut d'AO invalide
 * TransitionStatutOffreInvalideException      → 422  transition de statut d'offre invalide
 * </pre>
 */
@RestControllerAdvice(assignableTypes = {
        AppelOffresController.class,
        EspaceFournisseurAppelOffresController.class,
        EspaceFournisseurOffresController.class })
public class AppelOffreExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(AppelOffreExceptionHandler.class);

    @ExceptionHandler(AppelOffreIntrouvableException.class)
    public ResponseEntity<Map<String, Object>> handleAppelOffreIntrouvable(AppelOffreIntrouvableException ex) {
        return build(HttpStatus.NOT_FOUND, ex);
    }

    @ExceptionHandler(OffreFournisseurIntrouvableException.class)
    public ResponseEntity<Map<String, Object>> handleOffreIntrouvable(OffreFournisseurIntrouvableException ex) {
        return build(HttpStatus.NOT_FOUND, ex);
    }

    @ExceptionHandler(ReferenceAppelOffreDejaUtiliseeException.class)
    public ResponseEntity<Map<String, Object>> handleReferenceDejaUtilisee(
            ReferenceAppelOffreDejaUtiliseeException ex) {
        return build(HttpStatus.CONFLICT, ex);
    }

    @ExceptionHandler(AccesOffreRefuseException.class)
    public ResponseEntity<Map<String, Object>> handleAccesRefuse(AccesOffreRefuseException ex) {
        return build(HttpStatus.FORBIDDEN, ex);
    }

    @ExceptionHandler(AppelOffreNonPublieException.class)
    public ResponseEntity<Map<String, Object>> handleNonPublie(AppelOffreNonPublieException ex) {
        return build(HttpStatus.UNPROCESSABLE_ENTITY, ex);
    }

    @ExceptionHandler(DateClotureDepasseeException.class)
    public ResponseEntity<Map<String, Object>> handleDateClotureDepassee(DateClotureDepasseeException ex) {
        return build(HttpStatus.UNPROCESSABLE_ENTITY, ex);
    }

    @ExceptionHandler(OffreDejaSoumiseException.class)
    public ResponseEntity<Map<String, Object>> handleOffreDejaSoumise(OffreDejaSoumiseException ex) {
        return build(HttpStatus.CONFLICT, ex);
    }

    @ExceptionHandler(TransitionStatutAppelOffreInvalideException.class)
    public ResponseEntity<Map<String, Object>> handleTransitionAppelOffreInvalide(
            TransitionStatutAppelOffreInvalideException ex) {
        return build(HttpStatus.UNPROCESSABLE_ENTITY, ex);
    }

    @ExceptionHandler(TransitionStatutOffreInvalideException.class)
    public ResponseEntity<Map<String, Object>> handleTransitionOffreInvalide(
            TransitionStatutOffreInvalideException ex) {
        return build(HttpStatus.UNPROCESSABLE_ENTITY, ex);
    }

    private ResponseEntity<Map<String, Object>> build(HttpStatus status, SenPnaException ex) {
        log.warn("[APPEL_OFFRE] {} — {}", ex.getType(), ex.getMessage());
        return ResponseEntity.status(status).body(RestResponse.error(status, ex.getMessage(), ex.getType()));
    }
}
