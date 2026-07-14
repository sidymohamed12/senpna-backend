package ministere.sante.senpna.appeloffre.domain.exception;

import ministere.sante.senpna.shared.domain.exception.ErrorCategory;
import ministere.sante.senpna.shared.domain.exception.SenPnaException;

public class AppelOffreIntrouvableException extends SenPnaException {
    public AppelOffreIntrouvableException() {
        super("Appel d'offres introuvable", "APPEL_OFFRE_NOT_FOUND", ErrorCategory.NOT_FOUND);
    }
}
