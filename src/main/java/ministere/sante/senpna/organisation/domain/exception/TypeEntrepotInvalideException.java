package ministere.sante.senpna.organisation.domain.exception;

import ministere.sante.senpna.shared.domain.exception.ErrorCategory;
import ministere.sante.senpna.shared.domain.exception.SenPnaException;

/**
 * Levée lorsqu'une opération réservée aux PRA (création, modification,
 * désactivation, affectation) est appliquée à un entrepôt d'un autre type
 * (ex: la PNA centrale).
 */
public class TypeEntrepotInvalideException extends SenPnaException {
    public TypeEntrepotInvalideException() {
        super("Cette opération n'est autorisée que pour un entrepôt de type PRA", "INVALID_ENTREPOT_TYPE", ErrorCategory.BUSINESS_RULE);
    }
}
