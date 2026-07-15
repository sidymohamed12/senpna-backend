package ministere.sante.senpna.commandeachat.infrastructure.web.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record LigneCommandeAchatResponse(UUID id, UUID medicamentId, UUID conditionnementId,
        BigDecimal quantiteCommandee, BigDecimal prixUnitaire, String numeroLot, LocalDate dateFabrication,
        LocalDate dateExpiration, String certificatAnalyseUrl, BigDecimal quantiteExpediee,
        BigDecimal quantiteRecue, BigDecimal quantiteRefusee, String motifRefus) {
}
