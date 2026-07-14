package ministere.sante.senpna.appeloffre.domain.exception;

import ministere.sante.senpna.shared.domain.exception.SenPnaException;

/**
 * Levée lorsqu'un fournisseur tente de soumettre ou consulter une offre sur
 * un appel d'offres qui n'est pas (ou plus) publié — un appel d'offres en
 * brouillon, clôturé, attribué ou annulé n'est jamais visible ni
 * accessible en soumission côté fournisseur.
 */
public class AppelOffreNonPublieException extends SenPnaException {
    public AppelOffreNonPublieException() {
        super("Cet appel d'offres n'est pas ouvert à la soumission d'offres", "APPEL_OFFRE_NOT_PUBLISHED");
    }
}
