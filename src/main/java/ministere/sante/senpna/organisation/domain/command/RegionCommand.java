package ministere.sante.senpna.organisation.domain.command;

import java.time.Instant;
import java.util.UUID;

public final class RegionCommand {
    public record CreateRegionCommand(String code, String nom) {
    }

    public record GetRegionQuery(UUID regionId) {
    }

    public record RegionDetail(UUID id, String code, String nom, boolean actif, Instant createdAt,
            Instant updatedAt) {
    }
}
