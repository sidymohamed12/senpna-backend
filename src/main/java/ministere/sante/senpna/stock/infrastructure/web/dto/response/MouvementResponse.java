package ministere.sante.senpna.stock.infrastructure.web.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record MouvementResponse(
                UUID id,
                String typeMouvement,
                String sens,
                UUID entrepotSourceId,
                UUID entrepotDestinationId,
                UUID commandeId,
                UUID lotId,
                UUID medicamentId,
                BigDecimal quantite,
                Instant dateMouvement,
                String referenceDocument,
                String motif,
                UUID utilisateurId,
                Instant createdAt) {
}
