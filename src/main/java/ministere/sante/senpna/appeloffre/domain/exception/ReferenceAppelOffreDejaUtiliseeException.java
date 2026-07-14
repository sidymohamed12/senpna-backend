package ministere.sante.senpna.appeloffre.domain.exception;

import ministere.sante.senpna.shared.domain.exception.ErrorCategory;
import ministere.sante.senpna.shared.domain.exception.SenPnaException;

public class ReferenceAppelOffreDejaUtiliseeException extends SenPnaException {
    public ReferenceAppelOffreDejaUtiliseeException(String reference) {
        super("La référence d'appel d'offres '" + reference + "' est déjà utilisée",
                "APPEL_OFFRE_REFERENCE_ALREADY_USED", ErrorCategory.CONFLICT);
    }
}
