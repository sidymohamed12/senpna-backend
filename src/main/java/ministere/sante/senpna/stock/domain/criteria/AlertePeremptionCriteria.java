package ministere.sante.senpna.stock.domain.criteria;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Critère dédié aux alertes de péremption (cf. doc. métier §17) : ne
 * remonte que les lots {@code ACTIF} dont la date d'expiration est
 * comprise entre aujourd'hui et {@code dateLimite}.
 */
public record AlertePeremptionCriteria(LocalDate dateLimite, UUID medicamentId, UUID entrepotId) {
}
