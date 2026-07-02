package ministere.sante.senpna.fournisseur.domain.command;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class FournisseurCommands {

        private FournisseurCommands() {
        }

        // ── Commandes ────────────────────────────────────────────────────────

        public record CreateFournisseurCommand(String nom, String adresse, String telephone, String email,
                        String contactPrincipal) {
        }

        public record UpdateFournisseurCommand(UUID fournisseurId, String nom, String adresse, String telephone,
                        String email, String contactPrincipal) {
        }

        public record ActivateFournisseurCommand(UUID fournisseurId) {
        }

        public record DeactivateFournisseurCommand(UUID fournisseurId) {
        }

        // ── Requêtes ─────────────────────────────────────────────────────────

        public record GetFournisseurQuery(UUID fournisseurId) {
        }

        public record ListFournisseursQuery(String recherche, Boolean actif, Integer page, Integer size, String sortBy,
                        String sortDirection) {
        }

        // ── Résultats ────────────────────────────────────────────────────────

        public record FournisseurDetail(UUID id, String nom, String adresse, String telephone, String email,
                        String contactPrincipal, boolean actif, Instant createdAt, Instant updatedAt) {
        }

        public record FournisseurPage(List<FournisseurDetail> content, int page, int size, long totalElements,
                        int totalPages) {
        }
}
