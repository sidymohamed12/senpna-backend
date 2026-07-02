package ministere.sante.senpna.organisation.domain.port.out;

import java.util.UUID;

/**
 * Projection en lecture de l'affectation organisationnelle d'un
 * utilisateur — ne fait pas partie de l'agrégat {@code User} (module
 * {@code auth}/{@code utilisateurs}) : le module {@code organisation} lit
 * et modifie uniquement ces deux colonnes de la table {@code users} au
 * travers d'un port dédié, sans dépendre des autres bounded contexts.
 */
public record UserAffectationView(UUID userId, UUID entrepotId, UUID structureSanitaireId) {
}
