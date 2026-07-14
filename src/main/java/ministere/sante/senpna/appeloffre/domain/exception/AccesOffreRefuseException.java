package ministere.sante.senpna.appeloffre.domain.exception;

import ministere.sante.senpna.shared.domain.exception.SenPnaException;

/**
 * Levée lorsqu'un fournisseur tente d'accéder à une offre qui ne lui
 * appartient pas. Volontairement générique côté sécurité : n'expose pas
 * si l'offre existe pour un autre fournisseur. Traitée en 403 par le
 * handler web.
 */
public class AccesOffreRefuseException extends SenPnaException {
    public AccesOffreRefuseException() {
        super("Vous n'êtes pas autorisé à accéder à cette offre", "OFFRE_FOURNISSEUR_ACCESS_DENIED");
    }
}
