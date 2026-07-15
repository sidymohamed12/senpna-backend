package ministere.sante.senpna.commandeachat.infrastructure.web.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

public record GenererAvisExpeditionRequest(

        @NotNull(message = "La date d'expédition est obligatoire") LocalDate dateExpedition,

        @Size(max = 150, message = "Le transporteur ne peut pas dépasser 150 caractères") String transporteur,

        @Size(max = 100, message = "Le numéro de suivi ne peut pas dépasser 100 caractères") String numeroSuivi,

        LocalDate dateLivraisonEstimee,

        @NotEmpty(message = "Au moins une ligne doit être expédiée") @Valid List<InfoExpeditionLigneRequest> lignes) {
}
