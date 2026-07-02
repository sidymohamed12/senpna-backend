package ministere.sante.senpna.utilisateurs.domain.exception;

import ministere.sante.senpna.shared.domain.exception.ValidationException;

public class EntrepotNonApplicableException extends ValidationException {
    public EntrepotNonApplicableException() {
        super("Aucun entrepôt n'est attendu pour le(s) rôle(s) demandé(s)", "ENTREPOT_NOT_APPLICABLE");
    }
}
