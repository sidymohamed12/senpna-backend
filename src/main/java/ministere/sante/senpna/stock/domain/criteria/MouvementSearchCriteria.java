package ministere.sante.senpna.stock.domain.criteria;

import ministere.sante.senpna.stock.domain.valueobject.SensMouvement;
import ministere.sante.senpna.stock.domain.valueobject.TypeMouvement;

import java.time.Instant;
import java.util.UUID;

public record MouvementSearchCriteria(UUID lotId, UUID medicamentId, UUID entrepotId, TypeMouvement typeMouvement,
        SensMouvement sens, UUID utilisateurId, Instant dateDebut, Instant dateFin) {

    public static MouvementSearchCriteria vide() {
        return new MouvementSearchCriteria(null, null, null, null, null, null, null, null);
    }
}
