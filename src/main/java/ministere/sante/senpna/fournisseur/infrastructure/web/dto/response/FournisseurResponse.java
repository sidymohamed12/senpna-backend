package ministere.sante.senpna.fournisseur.infrastructure.web.dto.response;

import java.time.Instant;
import java.util.UUID;

public record FournisseurResponse(
                UUID id,
                String nom,
                String adresse,
                String telephone,
                String email,
                String contactPrincipal,
                boolean actif,
                Instant createdAt,
                Instant updatedAt) {
}
