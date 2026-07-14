package ministere.sante.senpna.appeloffre.infrastructure.web.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

public record CreateAppelOffreRequest(

        @NotBlank(message = "La référence est obligatoire") @Size(max = 50, message = "La référence ne peut pas dépasser 50 caractères") String reference,

        @NotBlank(message = "L'objet est obligatoire") @Size(max = 255, message = "L'objet ne peut pas dépasser 255 caractères") String objet,

        @NotNull(message = "La date de clôture est obligatoire") @Future(message = "La date de clôture doit être future") LocalDate dateCloture,

        @NotEmpty(message = "L'appel d'offres doit contenir au moins une ligne") @Valid List<LigneAppelOffreRequest> lignes) {
}
