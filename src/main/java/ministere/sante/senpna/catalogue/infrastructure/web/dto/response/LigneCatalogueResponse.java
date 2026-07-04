package ministere.sante.senpna.catalogue.infrastructure.web.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record LigneCatalogueResponse(UUID medicamentId, String code, String nomCommercial, String dci,
                String familleNom, String fabricant, String fournisseurNom, BigDecimal quantiteDisponible,
                int nombreLotsActifs, LocalDate prochaineDateExpiration, boolean enRupture,
                List<ConditionnementCatalogueResponse> conditionnements) {
}
