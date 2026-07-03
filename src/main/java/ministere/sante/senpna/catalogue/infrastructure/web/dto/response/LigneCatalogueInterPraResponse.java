package ministere.sante.senpna.catalogue.infrastructure.web.dto.response;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record LigneCatalogueInterPraResponse(UUID medicamentId, String code, String nomCommercial, String dci,
        String dosage, BigDecimal quantiteTotaleReseau, List<DisponibilitePraResponse> disponibilites) {
}
