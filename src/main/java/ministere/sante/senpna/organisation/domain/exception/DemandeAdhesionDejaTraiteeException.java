package ministere.sante.senpna.organisation.domain.exception;

import ministere.sante.senpna.shared.domain.exception.BusinessRuleException;

public class DemandeAdhesionDejaTraiteeException extends BusinessRuleException {
    public DemandeAdhesionDejaTraiteeException() {
        super("Cette demande d'adhésion a déjà été traitée (validée ou rejetée)", "ADHESION_ALREADY_PROCESSED");
    }
}
