package ministere.sante.senpna.stock.domain.command;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class StockCommands {

        private StockCommands() {
        }

        // ── Commandes ────────────────────────────────────────────────────────

        /** Entrée en stock (achat, transfert reçu, retour, don, ajustement positif). */
        public record EntreeStockCommand(UUID entrepotId, UUID lotId, BigDecimal quantite, String typeMouvement,
                        UUID commandeId, String referenceDocument, String motif, UUID utilisateurId) {
        }

        /**
         * Sortie de stock ciblée sur une ligne précise (entrepôt + lot) — pour
         * les pertes, casses, vols, péremptions, ajustements négatifs, ou une
         * expédition dont les lots ont déjà été sélectionnés manuellement.
         */
        public record SortieStockCommand(UUID entrepotId, UUID lotId, BigDecimal quantite, String typeMouvement,
                        boolean depuisReservation, UUID entrepotDestinationId, UUID commandeId,
                        String referenceDocument,
                        String motif, UUID utilisateurId) {
        }

        /**
         * Réservation automatique appliquant la règle FEFO (cf. doc. métier §8
         * et §9) : les lots {@code ACTIF} non expirés du médicament dans
         * l'entrepôt donné sont consommés par date d'expiration croissante
         * jusqu'à satisfaire {@code quantiteDemandee}.
         */
        public record ReserverStockFefoCommand(UUID entrepotId, UUID medicamentId, BigDecimal quantiteDemandee,
                        UUID commandeId) {
        }

        public record ReserverStockCommand(UUID entrepotId, UUID lotId, BigDecimal quantite, UUID commandeId) {
        }

        public record LibererReservationCommand(UUID entrepotId, UUID lotId, BigDecimal quantite, UUID commandeId,
                        String motif) {
        }

        public record DefinirSeuilAlerteCommand(UUID stockId, BigDecimal seuilAlerte) {
        }

        // ── Requêtes ─────────────────────────────────────────────────────────

        public record GetStockQuery(UUID stockId) {
        }

        public record ListStocksQuery(UUID entrepotId, UUID lotId, UUID medicamentId, Boolean ruptureUniquement,
                        Boolean seuilAtteintUniquement, Integer page, Integer size, String sortBy,
                        String sortDirection) {
        }

        // ── Résultats ────────────────────────────────────────────────────────

        public record StockDetail(UUID id, UUID entrepotId, UUID lotId, UUID medicamentId,
                        BigDecimal quantiteDisponible,
                        BigDecimal quantiteReservee, BigDecimal quantiteDisponibleALaVente,
                        BigDecimal quantiteEnCommande,
                        BigDecimal seuilAlerte, boolean enRupture, boolean seuilAtteint, Instant createdAt,
                        Instant updatedAt) {
        }

        public record StockPage(List<StockDetail> content, int page, int size, long totalElements, int totalPages) {
        }

        /** Allocation d'un lot particulier lors d'une réservation FEFO. */
        public record AllocationLot(UUID lotId, String numeroLot, BigDecimal quantiteAllouee) {
        }

        public record ReservationFefoResult(UUID entrepotId, UUID medicamentId, BigDecimal quantiteDemandee,
                        BigDecimal quantiteAllouee, List<AllocationLot> allocations) {

                public boolean estEntierementSatisfaite() {
                        return quantiteAllouee.compareTo(quantiteDemandee) >= 0;
                }
        }
}
