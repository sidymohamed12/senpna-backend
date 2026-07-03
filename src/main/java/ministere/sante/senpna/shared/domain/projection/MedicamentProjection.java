package ministere.sante.senpna.shared.domain.projection;

import java.util.UUID;

/**
 * Projection en lecture d'un médicament du référentiel national —
 * {@code shared} n'expose que les champs utiles à l'affichage/l'enrichissement
 * cross-feature (ex : catalogue), jamais l'agrégat {@code Medicament} complet,
 * dont {@code medicament} reste l'unique propriétaire.
 */
public record MedicamentProjection(UUID id, String code, String nomCommercial, String dci, String dosage,
                boolean necessiteOrdonnance, boolean actif) {
}
