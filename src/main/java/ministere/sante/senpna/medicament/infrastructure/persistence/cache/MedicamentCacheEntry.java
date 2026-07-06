package ministere.sante.senpna.medicament.infrastructure.persistence.cache;

import ministere.sante.senpna.medicament.domain.model.Medicament;
import ministere.sante.senpna.medicament.domain.valueobject.FamilleId;
import ministere.sante.senpna.medicament.domain.valueobject.FormeId;
import ministere.sante.senpna.medicament.domain.valueobject.MedicamentId;
import ministere.sante.senpna.medicament.domain.valueobject.TemperatureConservation;
import ministere.sante.senpna.medicament.domain.valueobject.VoieAdministration;

import java.time.Instant;
import java.util.UUID;

/**
 * Instantané JSON-sérialisable d'un {@link Medicament} — le cache
 * applicatif (Redis) ne connaît que des chaînes ; plutôt que de faire
 * porter des annotations Jackson par le modèle de domaine (violation de
 * la Clean Architecture : le domaine ne doit rien savoir de son mode de
 * (dé)serialisation), cette classe fait le pont, symétriquement à ce que
 * {@code MedicamentMapper} fait pour la persistance JPA.
 */
public record MedicamentCacheEntry(
        UUID id,
        String code,
        String nomCommercial,
        String dci,
        String dosage,
        UUID formeId,
        UUID familleId,
        VoieAdministration voieAdministration,
        TemperatureConservation temperatureConservation,
        String programmeSante,
        Integer delaiApprovisionnementJours,
        boolean necessiteOrdonnance,
        String fabricant,
        Integer stockMinimum,
        Integer stockMaximum,
        boolean actif,
        Instant createdAt,
        Instant updatedAt) {

    public static MedicamentCacheEntry from(Medicament medicament) {
        return new MedicamentCacheEntry(
                medicament.getId().getValue(),
                medicament.getCode(),
                medicament.getNomCommercial(),
                medicament.getDci(),
                medicament.getDosage(),
                medicament.getFormeId().getValue(),
                medicament.getFamilleId().getValue(),
                medicament.getVoieAdministration(),
                medicament.getTemperatureConservation(),
                medicament.getProgrammeSante(),
                medicament.getDelaiApprovisionnementJours(),
                medicament.isNecessiteOrdonnance(),
                medicament.getFabricant(),
                medicament.getStockMinimum(),
                medicament.getStockMaximum(),
                medicament.isActif(),
                medicament.getCreatedAt(),
                medicament.getUpdatedAt());
    }

    public Medicament toDomain() {
        return Medicament.reconstruct(
                MedicamentId.of(id),
                code,
                nomCommercial,
                dci,
                dosage,
                FormeId.of(formeId),
                FamilleId.of(familleId),
                voieAdministration,
                temperatureConservation,
                programmeSante,
                delaiApprovisionnementJours,
                necessiteOrdonnance,
                fabricant,
                stockMinimum,
                stockMaximum,
                actif,
                createdAt,
                updatedAt);
    }
}
