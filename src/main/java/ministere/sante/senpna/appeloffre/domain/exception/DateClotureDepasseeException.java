package ministere.sante.senpna.appeloffre.domain.exception;

import ministere.sante.senpna.shared.domain.exception.ErrorCategory;
import ministere.sante.senpna.shared.domain.exception.SenPnaException;

public class DateClotureDepasseeException extends SenPnaException {
    public DateClotureDepasseeException() {
        super("La date de clôture de cet appel d'offres est dépassée", "APPEL_OFFRE_DATE_CLOTURE_DEPASSEE", ErrorCategory.BUSINESS_RULE);
    }
}
