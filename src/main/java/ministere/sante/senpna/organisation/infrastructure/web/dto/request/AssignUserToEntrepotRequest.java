package ministere.sante.senpna.organisation.infrastructure.web.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AssignUserToEntrepotRequest(

        @NotNull(message = "L'identifiant de l'entrepôt est obligatoire")
        UUID entrepotId) {
}
