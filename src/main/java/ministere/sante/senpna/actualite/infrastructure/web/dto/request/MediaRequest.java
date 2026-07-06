package ministere.sante.senpna.actualite.infrastructure.web.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record MediaRequest(

        @NotBlank(message = "Le type de média est obligatoire") @Pattern(regexp = "IMAGE|VIDEO", message = "Le type de média doit être IMAGE ou VIDEO") String type,

        @NotBlank(message = "L'URL du média est obligatoire") @Size(max = 1000, message = "L'URL ne peut pas dépasser 1000 caractères") String url) {
}
