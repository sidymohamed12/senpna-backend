package ministere.sante.senpna.media.application.usecase;

import ministere.sante.senpna.media.application.dto.GenererPresignedUrlCommand;
import ministere.sante.senpna.media.application.dto.PresignedUrlResult;
import ministere.sante.senpna.media.domain.exception.ContentTypeNonAutoriseException;
import ministere.sante.senpna.media.domain.exception.TailleFichierDepasseeException;
import ministere.sante.senpna.media.domain.model.MediaType;
import ministere.sante.senpna.media.domain.port.in.GenererPresignedUrlUseCase;
import ministere.sante.senpna.media.domain.port.out.StoragePort;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implémentation du use case de génération de Presigned URL.
 *
 * <h3>Responsabilités</h3>
 * <ol>
 * <li>Valider le Content-Type contre les types autorisés pour le MediaType</li>
 * <li>Valider la taille déclarée contre le max autorisé</li>
 * <li>Générer une clé unique dans le bucket (UUID côté serveur)</li>
 * <li>Déléguer la génération de l'URL signée au StoragePort</li>
 * </ol>
 *
 * <h3>Sécurité</h3>
 * <p>
 * La clé est toujours générée côté serveur — le client ne contrôle jamais
 * le chemin de stockage. Cela prévient les attaques par path traversal
 * ou l'écrasement de fichiers existants.
 * </p>
 *
 */
@Service
public class GenererPresignedUrlUseCaseImpl implements GenererPresignedUrlUseCase {

    private final StoragePort storagePort;
    private final Duration presignedUrlTtl;

    public GenererPresignedUrlUseCaseImpl(
            StoragePort storagePort,
            @Value("${app.media.presigned-url-ttl-minutes:10}") int ttlMinutes) {
        this.storagePort = storagePort;
        this.presignedUrlTtl = Duration.ofMinutes(ttlMinutes);
    }

    @Override
    public PresignedUrlResult generer(GenererPresignedUrlCommand command) {
        MediaType mediaType = command.mediaType();

        // ── Validation Content-Type ──────────────────────────────────────
        if (!mediaType.accepteContentType(command.contentType())) {
            String typesAutorises = mediaType.getContentTypesAutorises()
                    .stream()
                    .sorted()
                    .collect(Collectors.joining(", "));
            throw new ContentTypeNonAutoriseException(command.contentType(), typesAutorises);
        }

        // ── Validation taille ────────────────────────────────────────────
        if (command.tailleBytes() > mediaType.getMaxSizeBytes()) {
            throw new TailleFichierDepasseeException(
                    command.tailleBytes(), mediaType.getMaxSizeBytes());
        }

        // ── Génération de la clé ─────
        String extension = extensionPourContentType(command.contentType());
        String key = mediaType.getPrefixe() + UUID.randomUUID() + "." + extension;

        // ── Délégation au StoragePort ────────────────────────────────────
        StoragePort.PresignedUploadResult storageResult = storagePort.genererPresignedUpload(
                new StoragePort.PresignedUploadParams(
                        key,
                        command.contentType(),
                        mediaType.getMaxSizeBytes(),
                        presignedUrlTtl));

        return new PresignedUrlResult(
                storageResult.uploadUrl(),
                storageResult.publicUrl(),
                storageResult.key(),
                storageResult.expiresAt());
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    private String extensionPourContentType(String contentType) {
        return switch (contentType) {
            case "image/png" -> "png";
            case "image/webp" -> "webp";
            case "image/gif" -> "gif";
            case "video/mp4" -> "mp4";
            case "application/pdf" -> "pdf";
            case "image/jpeg", "image/jpg" -> "jpg";
            default -> "jpg";
        };
    }
}
