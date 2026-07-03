package ministere.sante.senpna.medicament.infrastructure.web.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import ministere.sante.senpna.medicament.domain.valueobject.TemperatureConservation;
import ministere.sante.senpna.medicament.domain.valueobject.VoieAdministration;

import java.util.UUID;

public record CreateMedicamentRequest(

        @NotBlank(message = "Le code du médicament est obligatoire") @Pattern(regexp = "^[A-Za-z0-9_-]{2,30}$", message = "Le code doit être alphanumérique (2 à 30 caractères)") String code,

        @NotBlank(message = "Le nom commercial est obligatoire") @Size(max = 200, message = "Le nom commercial ne peut pas dépasser 200 caractères") String nomCommercial,

        @NotBlank(message = "La DCI est obligatoire") @Size(max = 200, message = "La DCI ne peut pas dépasser 200 caractères") String dci,

        @NotBlank(message = "Le dosage est obligatoire") @Size(max = 50, message = "Le dosage ne peut pas dépasser 50 caractères") String dosage,

        @NotNull(message = "La forme pharmaceutique est obligatoire") UUID formeId,

        @NotNull(message = "La famille thérapeutique est obligatoire") UUID familleId,

        VoieAdministration voieAdministration,

        TemperatureConservation temperatureConservation,

        @Size(max = 150, message = "Le programme de santé ne peut pas dépasser 150 caractères") String programmeSante,

        @PositiveOrZero(message = "Le délai d'approvisionnement ne peut pas être négatif") Integer delaiApprovisionnementJours,

        boolean necessiteOrdonnance,

        @Size(max = 150, message = "Le fabricant ne peut pas dépasser 150 caractères") String fabricant,

        @PositiveOrZero(message = "Le seuil minimum ne peut pas être négatif") Integer stockMinimum,

        @Min(value = 0, message = "Le seuil maximum ne peut pas être négatif") Integer stockMaximum) {
}
