package ministere.sante.senpna.organisation.domain.command;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import ministere.sante.senpna.organisation.domain.valueobject.TypeEntrepot;

public final class Entrepot {

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
}
