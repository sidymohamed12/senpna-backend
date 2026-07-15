package ministere.sante.senpna.commandeachat.infrastructure.web.dto.response;

import ministere.sante.senpna.commandeachat.domain.valueobject.StatutCommandeAchat;

import java.time.Instant;
import java.util.UUID;

public record CommandeAchatSummaryResponse(UUID id, String reference, UUID fournisseurId,
        StatutCommandeAchat statut, int nombreLignes, Instant createdAt) {
}
