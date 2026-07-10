package ministere.sante.senpna.medicament.domain.command;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class FamilleCommands {

    public record CreateFamilleCommand(String code, String libelle, String description) {
    }

    public record UpdateFamilleCommand(UUID familleId, String libelle, String description) {
    }

    public record ArchiveFamilleCommand(UUID familleId) {
    }

    public record DesarchiveFamilleCommand(UUID familleId) {
    }

    public record GetFamilleQuery(UUID familleId) {
    }

    public record ListFamillesQuery(String recherche, Boolean actif, Integer page, Integer size, String sortBy,
            String sortDirection) {
    }

    public record FamilleDetail(UUID id, String code, String libelle, String description, boolean actif,
            Instant createdAt, Instant updatedAt) {
    }

    public record FamillePage(List<FamilleDetail> content, int page, int size, long totalElements,
            int totalPages) {
    }

}
