package ministere.sante.senpna.shared.domain.projection;

import java.util.UUID;

/**
 * Projection en lecture d'une structure sanitaire — {@code regionId} et
 * {@code praId} sont les seuls champs organisationnels exposés
 * cross-feature (résolution de portée régionale, cf. {@code catalogue}),
 * le reste de l'agrégat {@code StructureSanitaire} restant la propriété
 * exclusive du module {@code organisation}.
 */
public record StructureSanitaireProjection(UUID id, String code, String nom, UUID regionId, UUID praId,
                boolean actif) {
}
