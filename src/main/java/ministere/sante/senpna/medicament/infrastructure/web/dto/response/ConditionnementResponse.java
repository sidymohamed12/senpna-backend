package ministere.sante.senpna.medicament.infrastructure.web.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ConditionnementResponse(
        UUID id,
        UUID medicamentId,
        String nom,
        int niveau,
        BigDecimal quantiteUniteBase,
        boolean estUniteBase,
        BigDecimal prixAchat,
        BigDecimal prixVente,
        boolean actif,
        Instant createdAt,
        Instant updatedAt) {
}
