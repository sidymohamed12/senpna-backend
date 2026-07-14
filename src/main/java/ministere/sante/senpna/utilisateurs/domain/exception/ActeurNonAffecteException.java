package ministere.sante.senpna.utilisateurs.domain.exception;

import ministere.sante.senpna.shared.domain.exception.ErrorCategory;
import ministere.sante.senpna.shared.domain.exception.SenPnaException;

/**
 * Levée lorsqu'un acteur régional (PRA), qui doit lui-même appartenir à
 * un entrepôt pour créer un compte subalterne, n'est rattaché à aucun
 * entrepôt — cas défensif qui ne devrait normalement pas se produire si
 * les règles de création sont respectées.
 */
public class ActeurNonAffecteException extends SenPnaException {
    public ActeurNonAffecteException() {
        super("Vous devez être rattaché à un entrepôt pour créer ce compte", "ACTEUR_NON_AFFECTE", ErrorCategory.BUSINESS_RULE);
    }
}
