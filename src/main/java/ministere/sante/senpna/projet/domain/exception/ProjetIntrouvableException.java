package ministere.sante.senpna.projet.domain.exception;

import ministere.sante.senpna.shared.domain.exception.NotFoundException;

public class ProjetIntrouvableException extends NotFoundException {
    public ProjetIntrouvableException() {
        super("Projet introuvable", "PROJET_NOT_FOUND");
    }
}
