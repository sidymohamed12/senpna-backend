package ministere.sante.senpna.medicament.infrastructure.web.dto.response;

import ministere.sante.senpna.medicament.domain.valueobject.TemperatureConservation;
import ministere.sante.senpna.medicament.domain.valueobject.VoieAdministration;

import java.time.Instant;
import java.util.UUID;

public record MedicamentResponse(
        UUID id,
        String code,
        String nomCommercial,
        String dci,
        String dosage,
        UUID formeId,
        String formeLibelle,
        UUID familleId,
        String familleLibelle,
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
}
