package ministere.sante.senpna.media.web.request;

import ministere.sante.senpna.media.domain.model.MediaType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PresignedUrlRequest(

                @NotNull(message = "Le type de média est obligatoire " +
                                "(valeurs : ACTUALITE, PROJET, MEDIATHEQUE, FICHE_DE_POSTE, CV, LETTRE_DE_MOTIVATION)") MediaType mediaType,

                @NotBlank(message = "Le type de contenu est obligatoire " +
                                "(ex: image/jpeg, image/png, image/webp)") String contentType,

                @Min(value = 1, message = "La taille déclarée doit être positive") long tailleBytes

) {
}
