package ministere.sante.senpna.medicament.domain.command;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class FormeCommands {

    public record CreateFormeCommand(String code, String libelle, String description) {
    }

    public record UpdateFormeCommand(UUID formeId, String libelle, String description) {
    }

    public record ArchiveFormeCommand(UUID formeId) {
    }

    public record DesarchiveFormeCommand(UUID formeId) {
    }

    public record GetFormeQuery(UUID formeId) {
    }

    public record ListFormesQuery(String recherche, Boolean actif, Integer page, Integer size, String sortBy,
            String sortDirection) {
    }

    public record FormeDetail(UUID id, String code, String libelle, String description, boolean actif,
            Instant createdAt, Instant updatedAt) {
    }

    public record FormePage(List<FormeDetail> content, int page, int size, long totalElements, int totalPages) {
    }

}
