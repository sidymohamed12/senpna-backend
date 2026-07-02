package ministere.sante.senpna.utilisateurs.domain.exception;

import ministere.sante.senpna.shared.domain.exception.ForbiddenException;

/**
 * Levée lorsqu'un acteur de portée régionale (PRA) tente de gérer
 * (créer, modifier, activer, désactiver, attribuer/retirer un rôle) un
 * compte de portée nationale (PNA) ou un compte administrateur pair
 * ({@code ADMIN_PRA}) — évite qu'un administrateur régional s'octroie ou
 * manipule des privilèges qui dépassent sa portée hiérarchique, y compris
 * envers un autre {@code ADMIN_PRA} d'une région différente.
 */
public class GestionUtilisateurInterditeException extends ForbiddenException {
    public GestionUtilisateurInterditeException() {
        super("Vous n'êtes pas autorisé à gérer ce compte : portée hiérarchique insuffisante",
                "USER_MANAGEMENT_FORBIDDEN");
    }
}
