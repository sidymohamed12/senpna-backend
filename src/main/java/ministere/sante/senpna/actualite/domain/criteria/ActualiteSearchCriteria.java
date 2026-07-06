package ministere.sante.senpna.actualite.domain.criteria;

import ministere.sante.senpna.actualite.domain.valueobject.CategorieActualite;
import ministere.sante.senpna.actualite.domain.valueobject.StatutActualite;

public record ActualiteSearchCriteria(String recherche, CategorieActualite categorie, StatutActualite statut) {

    public static ActualiteSearchCriteria vide() {
        return new ActualiteSearchCriteria(null, null, null);
    }
}
