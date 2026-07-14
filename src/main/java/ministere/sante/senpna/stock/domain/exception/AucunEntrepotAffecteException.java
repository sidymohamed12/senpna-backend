package ministere.sante.senpna.stock.domain.exception;

import ministere.sante.senpna.shared.domain.exception.ErrorCategory;
import ministere.sante.senpna.shared.domain.exception.SenPnaException;

/**
 * Levée lorsqu'un acteur PRA (rôle régional) tente une opération scopée
 * par entrepôt alors que son compte n'est affecté à aucun entrepôt —
 * situation anormale (un rôle PRA doit toujours être affecté), traitée
 * défensivement plutôt que de lever une NullPointerException plus loin.
 */
public class AucunEntrepotAffecteException extends SenPnaException {
    public AucunEntrepotAffecteException() {
        super("Votre compte n'est affecté à aucun entrepôt : contactez un administrateur",
                "NO_ENTREPOT_ASSIGNED", ErrorCategory.FORBIDDEN);
    }
}
