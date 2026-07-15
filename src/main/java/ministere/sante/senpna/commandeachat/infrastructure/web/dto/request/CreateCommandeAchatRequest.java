package ministere.sante.senpna.commandeachat.infrastructure.web.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

public record CreateCommandeAchatRequest(

        @NotBlank(message = "La référence est obligatoire") @Size(max = 50, message = "La référence ne peut pas dépasser 50 caractères") String reference,

        @NotNull(message = "Le fournisseur est obligatoire") UUID fournisseurId,

        @NotNull(message = "L'entrepôt de destination est obligatoire") UUID entrepotDestinationId,

        @Size(max = 1000, message = "Le commentaire ne peut pas dépasser 1000 caractères") String commentaire,

        @NotEmpty(message = "La commande doit contenir au moins une ligne") @Valid List<LigneCommandeAchatRequest> lignes) {
}
