package ministere.sante.senpna.commandeachat.infrastructure.web.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record ReceptionnerCommandeAchatRequest(
        @NotEmpty(message = "Au moins une ligne doit être réceptionnée") @Valid List<InfoReceptionLigneRequest> lignes) {
}
