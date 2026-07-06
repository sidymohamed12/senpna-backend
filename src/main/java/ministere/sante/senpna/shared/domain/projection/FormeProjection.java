package ministere.sante.senpna.shared.domain.projection;

import java.util.UUID;

/**
 * Projection en lecture d'une forme pharmaceutique — {@code shared}
 * n'expose que le strict nécessaire à l'affichage cross-feature (ex :
 * libellé de la forme dans le détail d'un médicament), jamais l'agrégat
 * {@code Forme} complet, dont le module {@code medicament} reste l'unique
 * propriétaire. Symétrique à {@link RegionProjection} /
 * {@link FournisseurProjection}.
 */
public record FormeProjection(UUID id, String code, String libelle, boolean actif) {
    public FormeProjection {
        if (id == null || code == null || code.isBlank() || libelle == null || libelle.isBlank()) {
            throw new IllegalArgumentException("id, code et libellé sont obligatoires");
        }
    }
}
