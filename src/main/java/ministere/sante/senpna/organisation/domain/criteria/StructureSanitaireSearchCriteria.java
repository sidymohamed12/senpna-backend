package ministere.sante.senpna.organisation.domain.criteria;

import ministere.sante.senpna.organisation.domain.valueobject.StatutAdhesion;
import ministere.sante.senpna.organisation.domain.valueobject.TypeStructureSanitaire;

import java.util.UUID;

public record StructureSanitaireSearchCriteria(
        String recherche,
        TypeStructureSanitaire type,
        UUID regionId,
        UUID praId,
        StatutAdhesion statutAdhesion,
        Boolean actif) {

    public static StructureSanitaireSearchCriteria vide() {
        return new StructureSanitaireSearchCriteria(null, null, null, null, null, null);
    }
}
