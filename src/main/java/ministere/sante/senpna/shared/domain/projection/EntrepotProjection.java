package ministere.sante.senpna.shared.domain.projection;

import java.util.UUID;

/**
 * Projection en lecture d'un entrepôt — {@code type} est une chaîne
 * ({@code "PRA"} ou {@code "PNA_CENTRAL"}) plutôt que l'énumération
 * {@code TypeEntrepot} du module {@code organisation}, précisément pour
 * que {@code shared} ne dépende jamais d'un type propre à une feature.
 */
public record EntrepotProjection(UUID id, String code, String nom, String type, UUID regionId, boolean actif) {
}
