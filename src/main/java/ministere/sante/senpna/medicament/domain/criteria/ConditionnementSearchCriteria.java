package ministere.sante.senpna.medicament.domain.criteria;

import java.util.UUID;

public record ConditionnementSearchCriteria(UUID medicamentId, Boolean actif) {

    public static ConditionnementSearchCriteria vide() {
        return new ConditionnementSearchCriteria(null, null);
    }
}
