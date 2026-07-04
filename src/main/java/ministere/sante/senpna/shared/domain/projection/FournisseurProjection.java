package ministere.sante.senpna.shared.domain.projection;

import java.util.UUID;

/**
 * Projection en lecture d'un fournisseur — {@code shared} n'expose que le
 * strict nécessaire à l'affichage cross-feature (ex : nom du fournisseur
 * dans le catalogue), jamais l'agrégat {@code Fournisseur} complet, dont
 * le module {@code fournisseur} reste l'unique propriétaire.
 */
public record FournisseurProjection(UUID id, String nom, boolean actif) {
}
