package ministere.sante.senpna.catalogue.infrastructure.web.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record LigneCatalogueResponse(UUID medicamentId, String code, String nomCommercial, String dci,
        String dosage, boolean necessiteOrdonnance, BigDecimal quantiteDisponible, int nombreLotsActifs,
        LocalDate prochaineDateExpiration, BigDecimal prixVenteMoyen, boolean enRupture) {
}
