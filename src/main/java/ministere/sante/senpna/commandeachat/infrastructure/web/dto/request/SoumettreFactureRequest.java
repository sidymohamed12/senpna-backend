package ministere.sante.senpna.commandeachat.infrastructure.web.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record SoumettreFactureRequest(

        @NotBlank(message = "Le numéro de facture est obligatoire") @Size(max = 50, message = "Le numéro de facture ne peut pas dépasser 50 caractères") String numeroFacture,

        @NotNull(message = "Le montant est obligatoire") @DecimalMin(value = "0.01", message = "Le montant doit être strictement positif") BigDecimal montant,

        @NotNull(message = "La date d'émission est obligatoire") LocalDate dateEmission,

        LocalDate dateEcheance,

        @Size(max = 100, message = "Référence de pièce jointe invalide") String pieceJointeMediaId) {
}
