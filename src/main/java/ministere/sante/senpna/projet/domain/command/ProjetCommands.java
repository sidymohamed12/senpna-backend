package ministere.sante.senpna.projet.domain.command;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class ProjetCommands {

        private ProjetCommands() {
        }

        // ── Commandes ────────────────────────────────────────────────────────

        public record CreateProjetCommand(String categorie, String nom, String description, List<String> objectifs,
                        List<String> impacts, String imageUrl) {
        }

        public record UpdateProjetCommand(UUID projetId, String categorie, String nom, String description,
                        List<String> objectifs, List<String> impacts, String imageUrl) {
        }

        public record PublierProjetCommand(UUID projetId) {
        }

        public record ArchiverProjetCommand(UUID projetId) {
        }

        public record DesactiverProjetCommand(UUID projetId) {
        }

        public record RemettreEnBrouillonProjetCommand(UUID projetId) {
        }

        // ── Requêtes ─────────────────────────────────────────────────────────

        public record GetProjetQuery(UUID projetId) {
        }

        public record ListProjetsQuery(String recherche, String categorie, String statut, Integer page, Integer size,
                        String sortBy, String sortDirection) {
        }

        // ── Résultats ────────────────────────────────────────────────────────

        public record ProjetDetail(UUID id, String categorie, String nom, String description, List<String> objectifs,
                        List<String> impacts, String imageUrl, String statut, Instant createdAt, Instant updatedAt) {
        }

        public record ProjetPage(List<ProjetDetail> content, int page, int size, long totalElements, int totalPages) {
        }
}
