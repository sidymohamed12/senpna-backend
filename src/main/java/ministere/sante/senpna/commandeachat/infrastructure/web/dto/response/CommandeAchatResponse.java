package ministere.sante.senpna.commandeachat.infrastructure.web.dto.response;

import ministere.sante.senpna.commandeachat.domain.valueobject.StatutCommandeAchat;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record CommandeAchatResponse(UUID id, String reference, UUID fournisseurId, UUID entrepotDestinationId,
        StatutCommandeAchat statut, List<LigneCommandeAchatResponse> lignes, Instant dateAccuseReceptionFournisseur,
        Integer delaiLivraisonConfirmeJours, LocalDate dateLivraisonConfirmee,
        AvisExpeditionResponse avisExpedition, String motifRejet, String commentaire, Instant createdAt,
        Instant updatedAt) {
}
