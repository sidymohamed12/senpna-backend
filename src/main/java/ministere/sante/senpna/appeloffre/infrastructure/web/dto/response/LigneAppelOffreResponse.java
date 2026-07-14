package ministere.sante.senpna.appeloffre.infrastructure.web.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

public record LigneAppelOffreResponse(UUID id, UUID medicamentId, String designation, BigDecimal quantiteEstimee,
        String uniteBase) {
}
