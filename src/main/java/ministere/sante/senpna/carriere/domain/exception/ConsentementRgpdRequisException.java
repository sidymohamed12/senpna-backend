package ministere.sante.senpna.carriere.domain.exception;

import ministere.sante.senpna.shared.domain.exception.ErrorCategory;
import ministere.sante.senpna.shared.domain.exception.SenPnaException;

public class ConsentementRgpdRequisException extends SenPnaException {
    public ConsentementRgpdRequisException() {
        super("Le consentement au traitement des données personnelles (RGPD) est obligatoire pour postuler",
                "CONSENTEMENT_RGPD_REQUIS", ErrorCategory.VALIDATION);
    }
}
