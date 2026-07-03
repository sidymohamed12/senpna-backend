package ministere.sante.senpna.stock.domain.command;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class MouvementStockCommands {

        private MouvementStockCommands() {
        }

        // ── Requêtes ─────────────────────────────────────────────────────────

        public record GetMouvementQuery(UUID mouvementId) {
        }

        public record ListMouvementsQuery(UUID lotId, UUID medicamentId, UUID entrepotId, String typeMouvement,
                        String sens, UUID utilisateurId, Instant dateDebut, Instant dateFin, Integer page, Integer size,
                        String sortBy, String sortDirection) {
        }

        // ── Résultats ────────────────────────────────────────────────────────

        public record MouvementDetail(UUID id, String typeMouvement, String sens, UUID entrepotSourceId,
                        UUID entrepotDestinationId, UUID commandeId, UUID lotId, UUID medicamentId, BigDecimal quantite,
                        Instant dateMouvement, String referenceDocument, String motif, UUID utilisateurId,
                        Instant createdAt) {
        }

        public record MouvementPage(List<MouvementDetail> content, int page, int size, long totalElements,
                        int totalPages) {
        }
}
