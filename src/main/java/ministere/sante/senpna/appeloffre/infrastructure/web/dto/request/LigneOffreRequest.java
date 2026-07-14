package ministere.sante.senpna.appeloffre.infrastructure.web.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record LigneOffreRequest(

        @NotNull(message = "La ligne d'appel d'offres référencée est obligatoire") UUID ligneAppelOffreId,

        @NotNull(message = "Le prix unitaire proposé est obligatoire") @DecimalMin(value = "0.01", message = "Le prix unitaire proposé doit être strictement positif") BigDecimal prixUnitaire,

        @NotNull(message = "Le délai de livraison proposé est obligatoire") @Min(value = 1, message = "Le délai de livraison doit être d'au moins 1 jour") Integer delaiLivraisonJours) {
}
