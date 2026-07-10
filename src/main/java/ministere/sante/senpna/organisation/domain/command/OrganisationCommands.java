package ministere.sante.senpna.organisation.domain.command;

import ministere.sante.senpna.organisation.domain.valueobject.StatutAdhesion;
import ministere.sante.senpna.organisation.domain.valueobject.TypeStructureSanitaire;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class OrganisationCommands {

        private OrganisationCommands() {
        }

        // ── Structure sanitaire ─────────────────────────────────────────────

        public record CreateStructureSanitaireCommand(String code, String nom, TypeStructureSanitaire type,
                        UUID regionId, String district, String adresse, String telephone, String email,
                        String responsableNom,
                        String responsablePrenom) {
        }

        public record UpdateStructureSanitaireCommand(UUID structureId, String nom, String district, String adresse,
                        String telephone, String email, String responsableNom, String responsablePrenom) {
        }

        public record ValidateAdhesionCommand(UUID structureId) {
        }

        public record RejectAdhesionCommand(UUID structureId, String motif) {
        }

        public record ActivateStructureSanitaireCommand(UUID structureId) {
        }

        public record DeactivateStructureSanitaireCommand(UUID structureId) {
        }

        public record AssignStructureToRegionCommand(UUID structureId, UUID regionId) {
        }

        public record AssignStructureToPraCommand(UUID structureId, UUID praId) {
        }

        public record GetStructureSanitaireQuery(UUID structureId) {
        }

        public record ListStructuresSanitairesQuery(String recherche, TypeStructureSanitaire type, UUID regionId,
                        UUID praId, StatutAdhesion statutAdhesion, Boolean actif, Integer page, Integer size,
                        String sortBy,
                        String sortDirection) {
        }

        public record StructureSanitaireDetail(UUID id, String code, String nom, TypeStructureSanitaire type,
                        UUID regionId, String regionNom, UUID praId, String praNom, String district, String adresse,
                        String telephone, String email, String responsableNom, String responsablePrenom,
                        StatutAdhesion statutAdhesion, String motifRejet, boolean actif, Instant createdAt,
                        Instant updatedAt) {
        }

        public record StructureSanitairePage(List<StructureSanitaireDetail> content, int page, int size,
                        long totalElements, int totalPages) {
        }

        // ── Affectation utilisateur ─────────────────────────────────────────

        public record AssignUserToEntrepotCommand(UUID userId, UUID entrepotId) {
        }

        public record AssignUserToStructureCommand(UUID userId, UUID structureId) {
        }

        public record UnassignUserCommand(UUID userId) {
        }

        public record UserAffectationDetail(UUID userId, UUID entrepotId, UUID structureSanitaireId) {
        }
}
