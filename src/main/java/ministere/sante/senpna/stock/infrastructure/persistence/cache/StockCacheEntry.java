package ministere.sante.senpna.stock.infrastructure.persistence.cache;

import ministere.sante.senpna.medicament.domain.valueobject.MedicamentId;
import ministere.sante.senpna.organisation.domain.valueobject.EntrepotId;
import ministere.sante.senpna.stock.domain.model.Stock;
import ministere.sante.senpna.stock.domain.valueobject.LotId;
import ministere.sante.senpna.stock.domain.valueobject.StockId;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Instantané JSON-sérialisable d'un {@link Stock}. Réservé aux lectures
 * non verrouillantes ({@code findById}, {@code findByEntrepotIdAndLotId})
 * — {@code findByEntrepotIdAndLotIdForUpdate} (verrou pessimiste utilisé
 * pendant réservation/entrée/sortie) ne doit jamais passer par ce cache,
 * sous peine de condition de course sur les quantités.
 */
public record StockCacheEntry(
        UUID id,
        UUID entrepotId,
        UUID lotId,
        UUID medicamentId,
        BigDecimal quantiteDisponible,
        BigDecimal quantiteReservee,
        BigDecimal quantiteEnCommande,
        BigDecimal seuilAlerte,
        Instant createdAt,
        Instant updatedAt) {

    public static StockCacheEntry from(Stock stock) {
        return new StockCacheEntry(
                stock.getId().getValue(),
                stock.getEntrepotId().getValue(),
                stock.getLotId().getValue(),
                stock.getMedicamentId().getValue(),
                stock.getQuantiteDisponible(),
                stock.getQuantiteReservee(),
                stock.getQuantiteEnCommande(),
                stock.getSeuilAlerte(),
                stock.getCreatedAt(),
                stock.getUpdatedAt());
    }

    public Stock toDomain() {
        return Stock.builder()
            .id(StockId.of(id))
            .entrepotId(EntrepotId.of(entrepotId))
            .lotId(LotId.of(lotId))
            .medicamentId(MedicamentId.of(medicamentId))
            .quantiteDisponible(quantiteDisponible)
            .quantiteReservee(quantiteReservee)
            .quantiteEnCommande(quantiteEnCommande)
            .seuilAlerte(seuilAlerte)
            .createdAt(createdAt)
            .updatedAt(updatedAt)
            .build();
    }
}
