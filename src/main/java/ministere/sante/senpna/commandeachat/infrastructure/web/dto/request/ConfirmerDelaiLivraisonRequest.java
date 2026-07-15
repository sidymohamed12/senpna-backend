package ministere.sante.senpna.commandeachat.infrastructure.web.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record ConfirmerDelaiLivraisonRequest(

        @NotNull(message = "Le délai de livraison est obligatoire") @Min(value = 1, message = "Le délai doit être d'au moins 1 jour") Integer delaiJours,

        @NotNull(message = "La date de livraison confirmée est obligatoire") @Future(message = "La date de livraison confirmée doit être future") LocalDate dateLivraisonConfirmee) {
}
