package ministere.sante.senpna.commandeachat.infrastructure.web.dto.response;

import java.time.LocalDate;

public record AvisExpeditionResponse(LocalDate dateExpedition, String transporteur, String numeroSuivi,
        LocalDate dateLivraisonEstimee) {
}
