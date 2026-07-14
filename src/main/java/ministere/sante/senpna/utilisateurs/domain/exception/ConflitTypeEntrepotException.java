package ministere.sante.senpna.utilisateurs.domain.exception;

import ministere.sante.senpna.shared.domain.exception.ErrorCategory;
import ministere.sante.senpna.shared.domain.exception.SenPnaException;

/**
 * Levée lorsque les rôles demandés pour un même compte exigent des types
 * d'entrepôt différents (ex: un rôle {@code PRA} et un rôle PNA
 * demandés simultanément) — combinaison invalide, un compte n'a qu'un
 * seul entrepôt.
 */
public class ConflitTypeEntrepotException extends SenPnaException {
    public ConflitTypeEntrepotException() {
        super("Les rôles demandés exigent des types d'entrepôt incompatibles entre eux", "ENTREPOT_TYPE_CONFLICT", ErrorCategory.VALIDATION);
    }
}
