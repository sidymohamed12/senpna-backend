package ministere.sante.senpna.appeloffre.infrastructure.web.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record LigneAppelOffreRequest(

        @NotNull(message = "Le médicament est obligatoire") UUID medicamentId,

        @NotBlank(message = "La désignation est obligatoire") String designation,

        @NotNull(message = "La quantité estimée est obligatoire") @DecimalMin(value = "0.01", message = "La quantité estimée doit être strictement positive") BigDecimal quantiteEstimee,

        @NotBlank(message = "L'unité de base est obligatoire") String uniteBase) {
}
