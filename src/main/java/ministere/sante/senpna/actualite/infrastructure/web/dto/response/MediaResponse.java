package ministere.sante.senpna.actualite.infrastructure.web.dto.response;

import java.util.UUID;

public record MediaResponse(UUID id, String type, String url, int ordre) {
}
