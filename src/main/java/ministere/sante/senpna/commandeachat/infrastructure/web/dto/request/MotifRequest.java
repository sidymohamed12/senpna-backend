package ministere.sante.senpna.commandeachat.infrastructure.web.dto.request;

import jakarta.validation.constraints.Size;

/** Corps générique pour les endpoints de rejet (commande, facture). */
public record MotifRequest(@Size(max = 500, message = "Le motif ne peut pas dépasser 500 caractères") String motif) {
}
