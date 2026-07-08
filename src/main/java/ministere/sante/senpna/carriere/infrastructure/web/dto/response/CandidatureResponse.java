package ministere.sante.senpna.carriere.infrastructure.web.dto.response;

import java.time.Instant;
import java.util.UUID;

public record CandidatureResponse(
        UUID id,
        UUID opportuniteId,
        String civilite,
        String nomComplet,
        String email,
        String telephone,
        String cvUrl,
        String lettreMotivationUrl,
        String messageComplementaire,
        boolean consentementRgpd,
        Instant dateCandidature) {
}
