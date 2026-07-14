package ministere.sante.senpna.appeloffre.domain.exception;

import ministere.sante.senpna.shared.domain.exception.NotFoundException;

public class OffreFournisseurIntrouvableException extends NotFoundException {
    public OffreFournisseurIntrouvableException() {
        super("Offre introuvable", "OFFRE_FOURNISSEUR_NOT_FOUND");
    }
}
