package ministere.sante.senpna.stock.infrastructure.persistence.cache;

import ministere.sante.senpna.fournisseur.domain.valueobject.FournisseurId;
import ministere.sante.senpna.medicament.domain.valueobject.MedicamentId;
import ministere.sante.senpna.stock.domain.model.Lot;
import ministere.sante.senpna.stock.domain.valueobject.LotId;
import ministere.sante.senpna.stock.domain.valueobject.StatutLot;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Instantané JSON-sérialisable d'un {@link Lot} — pont entre le cache
 * applicatif (Redis, clé/valeur) et le modèle de domaine, symétrique à
 * {@link MedicamentCacheEntry} du module {@code medicament}.
 */
public record LotCacheEntry(
        UUID id,
        String numeroLot,
        UUID medicamentId,
        UUID fournisseurId,
        LocalDate dateFabrication,
        LocalDate dateExpiration,
        BigDecimal prixAchat,
        BigDecimal prixVente,
        StatutLot statut,
        Instant createdAt,
        Instant updatedAt) {

    public static LotCacheEntry from(Lot lot) {
        return new LotCacheEntry(
                lot.getId().getValue(),
                lot.getNumeroLot(),
                lot.getMedicamentId().getValue(),
                lot.getFournisseurId().getValue(),
                lot.getDateFabrication(),
                lot.getDateExpiration(),
                lot.getPrixAchat(),
                lot.getPrixVente(),
                lot.getStatut(),
                lot.getCreatedAt(),
                lot.getUpdatedAt());
    }

    public Lot toDomain() {
        return Lot.builder()
            .id(LotId.of(id))
            .numeroLot(numeroLot)
            .medicamentId(MedicamentId.of(medicamentId))
            .fournisseurId(FournisseurId.of(fournisseurId))
            .dateFabrication(dateFabrication)
            .dateExpiration(dateExpiration)
            .prixAchat(prixAchat)
            .prixVente(prixVente)
            .statut(statut)
            .createdAt(createdAt)
            .updatedAt(updatedAt)
            .build();
    }
}
