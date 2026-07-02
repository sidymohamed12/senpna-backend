package ministere.sante.senpna.fournisseur.domain.exception;

import ministere.sante.senpna.shared.domain.exception.NotFoundException;

public class FournisseurIntrouvableException extends NotFoundException {
    public FournisseurIntrouvableException() {
        super("Fournisseur introuvable", "FOURNISSEUR_NOT_FOUND");
    }
}
