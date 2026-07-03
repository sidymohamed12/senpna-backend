package ministere.sante.senpna.medicament.domain.criteria;

import java.util.UUID;

public record MedicamentSearchCriteria(String recherche, UUID familleId, UUID formeId, Boolean actif) {

    public static MedicamentSearchCriteria vide() {
        return new MedicamentSearchCriteria(null, null, null, null);
    }
}
