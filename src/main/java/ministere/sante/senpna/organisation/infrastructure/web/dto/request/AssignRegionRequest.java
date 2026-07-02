package ministere.sante.senpna.organisation.infrastructure.web.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AssignRegionRequest(

        @NotNull(message = "L'identifiant de la région est obligatoire")
        UUID regionId) {
}
