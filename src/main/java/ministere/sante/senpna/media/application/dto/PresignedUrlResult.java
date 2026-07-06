package ministere.sante.senpna.media.application.dto;

import java.time.Instant;

public record PresignedUrlResult(
        String uploadUrl,
        String publicUrl,
        String key,
        Instant expiresAt) {
}
