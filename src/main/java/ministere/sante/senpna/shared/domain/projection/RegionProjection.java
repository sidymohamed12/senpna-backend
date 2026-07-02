package ministere.sante.senpna.shared.domain.projection;

import java.util.UUID;

public record RegionProjection(UUID id, String code, String nom, boolean actif) {
    public RegionProjection {
        if (id == null || code == null || code.isBlank() || nom == null || nom.isBlank()) {
            throw new IllegalArgumentException("id, code et nom sont obligatoires");
        }
    }
}
