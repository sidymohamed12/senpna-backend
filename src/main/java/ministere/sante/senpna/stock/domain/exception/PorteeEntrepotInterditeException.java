package ministere.sante.senpna.stock.domain.exception;

import ministere.sante.senpna.shared.domain.exception.ErrorCategory;
import ministere.sante.senpna.shared.domain.exception.SenPnaException;

/**
 * Levée lorsqu'un acteur (PNA ou PRA) tente de consulter ou modifier une
 * ligne de stock, un mouvement, ou un lot rattaché à un entrepôt autre
 * que le sien.
 *
 * <p>
 * Règle de portée du module {@code stock} (cf. {@code EntrepotScopeGuard}) :
 * un acteur PRA n'a jamais accès aux données d'un autre entrepôt qu'un
 * acteur PNA peut, lui, consulter (mais pas modifier) n'importe quel
 * entrepôt.
 * </p>
 */
public class PorteeEntrepotInterditeException extends SenPnaException {
    public PorteeEntrepotInterditeException() {
        super("Vous n'êtes pas autorisé à agir sur les données d'un autre entrepôt que le vôtre",
                "ENTREPOT_SCOPE_FORBIDDEN", ErrorCategory.FORBIDDEN);
    }
}
