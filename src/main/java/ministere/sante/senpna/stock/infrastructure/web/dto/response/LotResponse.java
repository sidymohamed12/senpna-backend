package ministere.sante.senpna.stock.infrastructure.web.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record LotResponse(
                UUID id,
                String numeroLot,
                UUID medicamentId,
                UUID fournisseurId,
                LocalDate dateFabrication,
                LocalDate dateExpiration,
                BigDecimal prixAchat,
                BigDecimal prixVente,
                String statut,
                boolean expire,
                long joursAvantExpiration,
                Instant createdAt,
                Instant updatedAt) {
}
