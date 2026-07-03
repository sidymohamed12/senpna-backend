package ministere.sante.senpna.catalogue.domain.exception;

import ministere.sante.senpna.shared.domain.exception.ForbiddenException;

/**
 * Levée par {@code CatalogueAccessGuard} lorsqu'un acteur tente de
 * consulter un catalogue auquel son rôle ou sa portée organisationnelle ne
 * lui donne pas accès :
 * <ul>
 * <li>catalogue PNA ou catalogue inter-PRA demandé par un acteur qui n'est
 * ni PNA ni PRA (typiquement {@code GESTIONNAIRE_STRUCTURE}) ;</li>
 * <li>catalogue régional demandé par un acteur non affecté à une PRA ou à
 * une structure sanitaire (donc à aucune région).</li>
 * </ul>
 */
public class CatalogueAccesRefuseException extends ForbiddenException {

    public CatalogueAccesRefuseException() {
        super("Vous n'êtes pas autorisé à consulter ce catalogue", "CATALOGUE_ACCESS_FORBIDDEN");
    }
}
