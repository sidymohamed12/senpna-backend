package ministere.sante.senpna.catalogue.infrastructure.web.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record DisponibilitePraResponse(UUID entrepotId, String codeEntrepot, String nomEntrepot, UUID regionId,
                String fournisseurNom, BigDecimal quantiteDisponible, LocalDate prochaineDateExpiration) {
}
