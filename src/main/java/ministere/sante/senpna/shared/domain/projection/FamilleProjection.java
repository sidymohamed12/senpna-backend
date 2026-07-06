package ministere.sante.senpna.shared.domain.projection;

import java.util.UUID;

/**
 * Projection en lecture d'une famille thérapeutique — {@code shared}
 * n'expose que le strict nécessaire à l'affichage cross-feature (ex :
 * nom de la famille dans une ligne de catalogue, cf.
 * {@code CatalogueEntryAssembler} / {@code MedicamentProjection}), jamais
 * l'agrégat {@code Famille} complet, dont le module {@code medicament}
 * reste l'unique propriétaire. Symétrique à {@link RegionProjection} /
 * {@link FournisseurProjection} / {@link FormeProjection}.
 */
public record FamilleProjection(UUID id, String code, String libelle, boolean actif) {
    public FamilleProjection {
        if (id == null || code == null || code.isBlank() || libelle == null || libelle.isBlank()) {
            throw new IllegalArgumentException("id, code et libellé sont obligatoires");
        }
    }
}
