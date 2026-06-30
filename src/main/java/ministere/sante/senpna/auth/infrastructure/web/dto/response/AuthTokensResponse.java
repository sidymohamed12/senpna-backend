package ministere.sante.senpna.auth.infrastructure.web.dto.response;

import java.util.Set;
import java.util.UUID;

public record AuthTokensResponse(
                String accessToken,
                String refreshToken,
                long expiresInSeconds,
                UUID userId,
                String nom,
                String prenom,
                String email,
                Set<String> roles) {
}
