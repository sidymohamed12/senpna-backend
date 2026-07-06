package ministere.sante.senpna.actualite.infrastructure.web.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record UpdateActualiteRequest(

                @NotBlank(message = "La catégorie est obligatoire") String categorie,

                @NotBlank(message = "Le titre est obligatoire") @Size(max = 200, message = "Le titre ne peut pas dépasser 200 caractères") String titre,

                @Size(max = 500, message = "La description ne peut pas dépasser 500 caractères") String description,

                @Size(max = 10, message = "Une actualité ne peut pas avoir plus de 10 médias") @Valid List<MediaRequest> medias,

                @Size(max = 20, message = "Une actualité ne peut pas avoir plus de 20 tags") List<@Size(max = 50, message = "Un tag ne peut pas dépasser 50 caractères") String> tags) {
}
