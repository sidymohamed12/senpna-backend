package ministere.sante.senpna.projet.domain.exception;

import ministere.sante.senpna.shared.domain.exception.ValidationException;

public class CategorieProjetInvalideException extends ValidationException {
    public CategorieProjetInvalideException(String valeur) {
        super("Catégorie de projet invalide : '" + valeur + "'", "CATEGORIE_PROJET_INVALIDE");
    }
}
