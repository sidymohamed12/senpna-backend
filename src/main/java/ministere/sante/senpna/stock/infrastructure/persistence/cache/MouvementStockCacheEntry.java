package ministere.sante.senpna.stock.infrastructure.persistence.cache;

import ministere.sante.senpna.medicament.domain.valueobject.MedicamentId;
import ministere.sante.senpna.organisation.domain.valueobject.EntrepotId;
import ministere.sante.senpna.stock.domain.model.MouvementStock;
import ministere.sante.senpna.stock.domain.valueobject.LotId;
import ministere.sante.senpna.stock.domain.valueobject.MouvementStockId;
import ministere.sante.senpna.stock.domain.valueobject.SensMouvement;
import ministere.sante.senpna.stock.domain.valueobject.TypeMouvement;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Instantané JSON-sérialisable d'un {@link MouvementStock}. Le journal
 * des mouvements est <em>append-only</em> (cf. Javadoc
 * {@code MouvementStockRepositoryPort} : « pas de mise à jour, seulement
 * des insertions ») — une fois écrit, un mouvement ne change plus jamais :
 * ce cache peut donc utiliser un TTL nettement plus long que les autres
 * ({@code app.cache.mouvement-ttl}, 6h par défaut) sans jamais servir de
 * donnée obsolète.
 */
public record MouvementStockCacheEntry(
        UUID id,
        TypeMouvement typeMouvement,
        SensMouvement sens,
        UUID entrepotSourceId,
        UUID entrepotDestinationId,
        UUID commandeId,
        UUID lotId,
        UUID medicamentId,
        BigDecimal quantite,
        Instant dateMouvement,
        String referenceDocument,
        String motif,
        UUID utilisateurId,
        Instant createdAt,
        Instant updatedAt) {

    public static MouvementStockCacheEntry from(MouvementStock mouvement) {
        return new MouvementStockCacheEntry(
                mouvement.getId().getValue(),
                mouvement.getTypeMouvement(),
                mouvement.getSens(),
                mouvement.getEntrepotSourceId() != null ? mouvement.getEntrepotSourceId().getValue() : null,
                mouvement.getEntrepotDestinationId() != null ? mouvement.getEntrepotDestinationId().getValue()
                        : null,
                mouvement.getCommandeId(),
                mouvement.getLotId().getValue(),
                mouvement.getMedicamentId().getValue(),
                mouvement.getQuantite(),
                mouvement.getDateMouvement(),
                mouvement.getReferenceDocument(),
                mouvement.getMotif(),
                mouvement.getUtilisateurId(),
                mouvement.getCreatedAt(),
                mouvement.getUpdatedAt());
    }

    public MouvementStock toDomain() {
        return MouvementStock.builder()
            .id(MouvementStockId.of(id))
            .typeMouvement(typeMouvement)
            .sens(sens)
            .entrepotSourceId(entrepotSourceId != null ? EntrepotId.of(entrepotSourceId) : null)
            .entrepotDestinationId(entrepotDestinationId != null ? EntrepotId.of(entrepotDestinationId) : null)
            .commandeId(commandeId)
            .lotId(LotId.of(lotId))
            .medicamentId(MedicamentId.of(medicamentId))
            .quantite(quantite)
            .dateMouvement(dateMouvement)
            .referenceDocument(referenceDocument)
            .motif(motif)
            .utilisateurId(utilisateurId)
            .createdAt(createdAt)
            .updatedAt(updatedAt)
            .build();
    }
}
