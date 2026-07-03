package ministere.sante.senpna.stock.infrastructure.web.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record CreerLotRequest(

                @NotBlank(message = "Le numéro de lot est obligatoire") @Size(max = 50, message = "Le numéro de lot ne peut pas dépasser 50 caractères") String numeroLot,

                @NotNull(message = "Le médicament est obligatoire") UUID medicamentId,

                @NotNull(message = "Le fournisseur est obligatoire") UUID fournisseurId,

                LocalDate dateFabrication,

                @NotNull(message = "La date d'expiration est obligatoire") LocalDate dateExpiration,

                @DecimalMin(value = "0", message = "Le prix d'achat ne peut pas être négatif") BigDecimal prixAchat,

                @DecimalMin(value = "0", message = "Le prix de vente ne peut pas être négatif") BigDecimal prixVente) {
}
