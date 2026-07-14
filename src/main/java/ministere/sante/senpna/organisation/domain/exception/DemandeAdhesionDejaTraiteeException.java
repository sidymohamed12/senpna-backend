package ministere.sante.senpna.organisation.domain.exception;

import ministere.sante.senpna.shared.domain.exception.ErrorCategory;
import ministere.sante.senpna.shared.domain.exception.SenPnaException;

public class DemandeAdhesionDejaTraiteeException extends SenPnaException {
    public DemandeAdhesionDejaTraiteeException() {
        super("Cette demande d'adhésion a déjà été traitée (validée ou rejetée)", "ADHESION_ALREADY_PROCESSED", ErrorCategory.BUSINESS_RULE);
    }
}
