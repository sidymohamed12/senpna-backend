package ministere.sante.senpna.shared.domain.projection;

import java.util.UUID;

public record RoleProjection(UUID id, String nom) {
    public RoleProjection {
        if (id == null || nom == null || nom.isBlank()) {
            throw new IllegalArgumentException("id et nom sont obligatoires");
        }
    }
}
