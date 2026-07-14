package ministere.sante.senpna.shared.domain.projection;

import java.util.UUID;

/**
 * Projection en lecture de l'affectation organisationnelle d'un
 * utilisateur — ne fait pas partie de l'agrégat {@code User} (module
 * {@code auth}/{@code utilisateurs}) : c'est une donnée transverse, lue et
 * modifiée aussi bien par {@code utilisateurs} (à la création d'un
 * compte) que par {@code organisation} (affectation/désaffectation
 * ultérieure), via un port dédié dans {@code shared} — aucun des deux
 * modules ne dépend de l'autre pour cette donnée.
 */
public record UserAffectationView(UUID userId, UUID entrepotId, UUID structureSanitaireId, UUID fournisseurId) {
}
