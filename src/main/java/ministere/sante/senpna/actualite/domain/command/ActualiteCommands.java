package ministere.sante.senpna.actualite.domain.command;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class ActualiteCommands {

        private ActualiteCommands() {
        }

        // ── Entrées imbriquées ──────────────────────────────────────────────

        /**
         * Média en entrée d'une commande de création/modification.
         *
         * @param type "IMAGE" ou "VIDEO"
         * @param url  URL publique du fichier uploadé ou URL externe video
         * 
         */
        public record MediaInput(String type, String url) {
        }

        // ── Commandes ────────────────────────────────────────────────────────

        public record CreateActualiteCommand(UUID auteurId, String categorie, String titre, String description,
                        List<MediaInput> medias, List<String> tags) {
        }

        public record UpdateActualiteCommand(UUID actualiteId, String categorie, String titre, String description,
                        List<MediaInput> medias, List<String> tags) {
        }

        public record PublierActualiteCommand(UUID actualiteId) {
        }

        public record DesactiverActualiteCommand(UUID actualiteId) {
        }

        public record RemettreEnBrouillonActualiteCommand(UUID actualiteId) {
        }

        // ── Requêtes ─────────────────────────────────────────────────────────

        public record GetActualiteQuery(UUID actualiteId) {
        }

        public record ListActualitesQuery(String recherche, String categorie, String statut, Integer page, Integer size,
                        String sortBy, String sortDirection) {
        }

        // ── Résultats ────────────────────────────────────────────────────────

        public record MediaDetail(UUID id, String type, String url, int ordre) {
        }

        public record ActualiteDetail(UUID id, String categorie, String titre, String description,
                        List<MediaDetail> medias, UUID auteurId, String auteurNom, List<String> tags, String statut,
                        Instant createdAt, Instant updatedAt) {
        }

        public record ActualitePage(List<ActualiteDetail> content, int page, int size, long totalElements,
                        int totalPages) {
        }
}
