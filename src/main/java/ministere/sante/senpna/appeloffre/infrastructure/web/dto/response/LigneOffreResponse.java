package ministere.sante.senpna.appeloffre.infrastructure.web.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

public record LigneOffreResponse(UUID id, UUID ligneAppelOffreId, BigDecimal prixUnitaire,
        Integer delaiLivraisonJours) {
}
