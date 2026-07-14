package ministere.sante.senpna.appeloffre.infrastructure.web.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record SoumettreOffreRequest(

        @Size(max = 1000, message = "Le commentaire ne peut pas dépasser 1000 caractères") String commentaire,

        @NotEmpty(message = "L'offre doit contenir au moins une ligne de prix") @Valid List<LigneOffreRequest> lignes) {
}
