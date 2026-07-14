package ministere.sante.senpna.appeloffre.infrastructure.web.dto.request;

import java.util.List;
import java.util.UUID;

public record AttribuerAppelOffreRequest(List<UUID> offresRetenuesIds, List<UUID> offresRejeteesIds) {
}
