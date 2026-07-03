package ministere.sante.senpna.stock.domain.command;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public final class LotCommands {

        private LotCommands() {
        }

        // ── Commandes ────────────────────────────────────────────────────────

        public record CreerLotCommand(String numeroLot, UUID medicamentId, UUID fournisseurId,
                        LocalDate dateFabrication, LocalDate dateExpiration, BigDecimal prixAchat,
                        BigDecimal prixVente) {
        }

        public record BloquerLotCommand(UUID lotId) {
        }

        public record DebloquerLotCommand(UUID lotId) {
        }

        public record ModifierPrixLotCommand(UUID lotId, BigDecimal prixAchat, BigDecimal prixVente) {
        }

        // ── Requêtes ─────────────────────────────────────────────────────────

        public record GetLotQuery(UUID lotId) {
        }

        public record ListLotsQuery(String recherche, UUID medicamentId, UUID fournisseurId, String statut,
                        Integer page, Integer size, String sortBy, String sortDirection) {
        }

        /**
         * Alertes de péremption (cf. doc. métier §17) — lots {@code ACTIF}
         * dont la date d'expiration se situe dans les {@code horizonJours} à
         * venir. Utilisée pour les paliers 12/6/3/1 mois.
         */
        public record AlertePeremptionQuery(int horizonJours, UUID medicamentId, Integer page, Integer size) {
        }

        // ── Résultats ────────────────────────────────────────────────────────

        public record LotDetail(UUID id, String numeroLot, UUID medicamentId, UUID fournisseurId,
                        LocalDate dateFabrication, LocalDate dateExpiration, BigDecimal prixAchat, BigDecimal prixVente,
                        String statut, boolean expire, long joursAvantExpiration, Instant createdAt,
                        Instant updatedAt) {
        }

        public record LotPage(List<LotDetail> content, int page, int size, long totalElements, int totalPages) {
        }
}
