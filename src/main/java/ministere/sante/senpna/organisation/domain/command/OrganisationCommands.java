package ministere.sante.senpna.organisation.domain.command;

import ministere.sante.senpna.organisation.domain.valueobject.StatutAdhesion;
import ministere.sante.senpna.organisation.domain.valueobject.TypeEntrepot;
import ministere.sante.senpna.organisation.domain.valueobject.TypeStructureSanitaire;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class OrganisationCommands {

    private OrganisationCommands() {
    }

    // ── Région ──────────────────────────────────────────────────────────

    public record CreateRegionCommand(String code, String nom) {
    }

    public record GetRegionQuery(UUID regionId) {
    }

    public record RegionDetail(UUID id, String code, String nom, boolean actif, Instant createdAt,
            Instant updatedAt) {
    }

    // ── Entrepôt / PRA ──────────────────────────────────────────────────

    public record CreatePraCommand(String code, String nom, UUID regionId, String adresse, String telephone) {
    }

    public record UpdatePraCommand(UUID entrepotId, UUID acteurId, String nom, String adresse, String telephone,
            UUID regionId) {
    }

    public record DeactivatePraCommand(UUID entrepotId, UUID acteurId) {
    }

    public record ActivatePraCommand(UUID entrepotId, UUID acteurId) {
    }

    public record GetEntrepotQuery(UUID entrepotId) {
    }

    public record ListEntrepotsQuery(String recherche, TypeEntrepot type, UUID regionId, Boolean actif,
            Integer page, Integer size, String sortBy, String sortDirection) {
    }

    public record EntrepotDetail(UUID id, String code, String nom, TypeEntrepot type, UUID regionId,
            String regionNom, String adresse, String telephone, UUID responsableUserId, boolean actif,
            Instant createdAt, Instant updatedAt) {
    }

    public record EntrepotPage(List<EntrepotDetail> content, int page, int size, long totalElements,
            int totalPages) {
    }

    // ── Structure sanitaire ─────────────────────────────────────────────

    public record CreateStructureSanitaireCommand(String code, String nom, TypeStructureSanitaire type,
            UUID regionId, String district, String adresse, String telephone, String email, String responsable) {
    }

    public record UpdateStructureSanitaireCommand(UUID structureId, String nom, String district, String adresse,
            String telephone, String email, String responsable) {
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
            UUID praId, StatutAdhesion statutAdhesion, Boolean actif, Integer page, Integer size, String sortBy,
            String sortDirection) {
    }

    public record StructureSanitaireDetail(UUID id, String code, String nom, TypeStructureSanitaire type,
            UUID regionId, String regionNom, UUID praId, String praNom, String district, String adresse,
            String telephone, String email, String responsable, StatutAdhesion statutAdhesion, String motifRejet,
            boolean actif, Instant createdAt, Instant updatedAt) {
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
