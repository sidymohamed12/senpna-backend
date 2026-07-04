package ministere.sante.senpna.catalogue.infrastructure.web.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

public record ConditionnementCatalogueResponse(UUID id, String nom, int niveau, BigDecimal quantiteUniteBase,
        BigDecimal prixAchat, BigDecimal prixVente) {
}
