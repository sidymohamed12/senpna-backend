package ministere.sante.senpna.organisation.domain.criteria;

import ministere.sante.senpna.organisation.domain.valueobject.TypeEntrepot;

import java.util.UUID;

public record EntrepotSearchCriteria(String recherche, TypeEntrepot type, UUID regionId, Boolean actif) {

    public static EntrepotSearchCriteria vide() {
        return new EntrepotSearchCriteria(null, null, null, null);
    }
}
