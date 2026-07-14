package ministere.sante.senpna.organisation.domain.exception;

import ministere.sante.senpna.shared.domain.exception.ErrorCategory;
import ministere.sante.senpna.shared.domain.exception.SenPnaException;

/**
 * Levée lorsqu'un utilisateur rattaché à une région tente de gérer un
 * entrepôt (PRA) d'une autre région — évite qu'un utilisateur d'une
 * région modifie les entrepôts d'une région dont il n'a pas la charge.
 * Ne s'applique pas aux utilisateurs non rattachés à une unité
 * organisationnelle régionale (typiquement les rôles PNA nationaux), qui
 * conservent un accès à toutes les régions.
 */
public class AccesRegionRefuseException extends SenPnaException {
    public AccesRegionRefuseException() {
        super("Vous n'êtes pas autorisé à gérer les entrepôts d'une autre région", "REGION_ACCESS_DENIED", ErrorCategory.FORBIDDEN);
    }
}
