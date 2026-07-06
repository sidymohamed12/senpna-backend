package ministere.sante.senpna.projet.domain.exception;

import ministere.sante.senpna.shared.domain.exception.ValidationException;

public class StatutProjetInvalideException extends ValidationException {
    public StatutProjetInvalideException(String valeur) {
        super("Statut de projet invalide : '" + valeur + "'", "STATUT_PROJET_INVALIDE");
    }
}
