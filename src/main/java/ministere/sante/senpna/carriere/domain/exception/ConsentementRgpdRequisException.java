package ministere.sante.senpna.carriere.domain.exception;

import ministere.sante.senpna.shared.domain.exception.ValidationException;

public class ConsentementRgpdRequisException extends ValidationException {
    public ConsentementRgpdRequisException() {
        super("Le consentement au traitement des données personnelles (RGPD) est obligatoire pour postuler",
                "CONSENTEMENT_RGPD_REQUIS");
    }
}
