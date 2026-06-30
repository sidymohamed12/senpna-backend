package ministere.sante.senpna.shared.domain.projection;

import java.util.UUID;

public record RoleProjection(UUID id, String code, String nom) {
    public RoleProjection {
        if (id == null || code == null || code.isBlank() || nom == null || nom.isBlank()) {
            throw new IllegalArgumentException("id, code et nom sont obligatoires");
        }
    }
}
