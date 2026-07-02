package ministere.sante.senpna.organisation.infrastructure.web.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AssignUserToStructureRequest(

        @NotNull(message = "L'identifiant de la structure sanitaire est obligatoire")
        UUID structureSanitaireId) {
}
