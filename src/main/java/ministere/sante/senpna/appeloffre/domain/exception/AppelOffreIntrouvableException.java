package ministere.sante.senpna.appeloffre.domain.exception;

import ministere.sante.senpna.shared.domain.exception.NotFoundException;

public class AppelOffreIntrouvableException extends NotFoundException {
    public AppelOffreIntrouvableException() {
        super("Appel d'offres introuvable", "APPEL_OFFRE_NOT_FOUND");
    }
}
