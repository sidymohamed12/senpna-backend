package ministere.sante.senpna.shared.domain.projection;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Projection en lecture d'un conditionnement commercialisable — un niveau
 * d'emballage (carton, boîte, plaquette...) d'un médicament, avec son
 * prix. {@code shared} n'expose que les conditionnements ayant un prix
 * défini : un conditionnement sans prix n'est pas proposé à la commande
 * (cf. {@code medicament.domain.model.Conditionnement}, seul propriétaire
 * de l'agrégat complet, y compris des conditionnements non tarifés).
 */
public record ConditionnementProjection(UUID id, UUID medicamentId, String nom, int niveau,
                BigDecimal quantiteUniteBase, boolean estUniteBase, BigDecimal prixAchat, BigDecimal prixVente) {
}
