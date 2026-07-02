package ministere.sante.senpna.organisation.infrastructure.web.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AssignPraRequest(

        @NotNull(message = "L'identifiant de la PRA est obligatoire")
        UUID praId) {
}
