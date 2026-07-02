package ministere.sante.senpna.organisation.infrastructure.web.dto.request;

import jakarta.validation.constraints.NotBlank;

public record RejectAdhesionRequest(

        @NotBlank(message = "Le motif du rejet est obligatoire")
        String motif) {
}
