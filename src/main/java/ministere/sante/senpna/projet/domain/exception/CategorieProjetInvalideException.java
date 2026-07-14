package ministere.sante.senpna.projet.domain.exception;

import ministere.sante.senpna.shared.domain.exception.ErrorCategory;
import ministere.sante.senpna.shared.domain.exception.SenPnaException;

public class CategorieProjetInvalideException extends SenPnaException {
    public CategorieProjetInvalideException(String valeur) {
        super("Catégorie de projet invalide : '" + valeur + "'", "CATEGORIE_PROJET_INVALIDE", ErrorCategory.VALIDATION);
    }
}
