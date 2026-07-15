package ministere.sante.senpna.commandeachat.infrastructure.web.dto.response;

import ministere.sante.senpna.commandeachat.domain.valueobject.StatutFacture;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record FactureResponse(UUID id, UUID commandeAchatId, UUID fournisseurId, String numeroFacture,
        BigDecimal montant, LocalDate dateEmission, LocalDate dateEcheance, String pieceJointeMediaId,
        StatutFacture statut, String motifRejet, Instant createdAt, Instant updatedAt) {
}
