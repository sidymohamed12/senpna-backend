package ministere.sante.senpna.media.infrastructure.stockage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ministere.sante.senpna.media.domain.port.out.StoragePort;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.io.InputStream;
import java.net.URI;
import java.time.Instant;

/**
 * Adaptateur Cloudflare R2 — stockage objet production (ADR-004).
 *
 * <h3>Presigned URL avec R2</h3>
 * <p>
 * R2 supporte les Presigned URLs via l'API AWS S3 v2.
 * L'URL générée permet au frontend d'uploader directement vers R2
 * avec les contraintes définies (Content-Type, taille max, TTL).
 * </p>
 *
 * <h3>Configuration CORS R2 requise</h3>
 * <p>
 * Pour que le navigateur puisse faire un PUT directement vers R2,
 * configurer dans le dashboard Cloudflare → R2 → bucket → CORS :
 * </p>
 * 
 * <pre>
 * [
 *   {
 *     "AllowedOrigins": ["https://app.restofacile.sn"],
 *     "AllowedMethods": ["PUT"],
 *     "AllowedHeaders": ["Content-Type", "Content-Length"],
 *     "MaxAgeSeconds": 3600
 *   }
 * ]
 * </pre>
 *
 * <p>
 * Ce bean est activé uniquement avec {@code @Profile({"prod", "dev"})} via
 * {@code StorageConfig}.
 * </p>
 */
public class R2StorageAdapter implements StoragePort {

        private static final Logger log = LoggerFactory.getLogger(R2StorageAdapter.class);

        private final S3Client s3Client;
        private final S3Presigner s3Presigner;
        private final String bucket;
        private final String publicUrl;
        private final String accountId;
        private final String accessKey;
        private final String secretKey;

        /**
         * @param s3Client  client AWS S3 v2 configuré avec l'endpoint R2
         * @param bucket    nom du bucket R2 (ex: {@code "resto-facile-media"})
         * @param publicUrl URL publique du bucket R2 (ex:
         *                  {@code "https://pub-xxx.r2.dev"})
         * @param accountId account ID Cloudflare (pour construire l'endpoint presigner)
         * @param accessKey access key R2
         * @param secretKey secret key R2
         */
        public R2StorageAdapter(S3Client s3Client, String bucket, String publicUrl,
                        String accountId, String accessKey, String secretKey) {
                this.s3Client = s3Client;
                this.bucket = bucket;
                this.accountId = accountId;
                this.accessKey = accessKey;
                this.secretKey = secretKey;
                this.publicUrl = publicUrl.endsWith("/")
                                ? publicUrl.substring(0, publicUrl.length() - 1)
                                : publicUrl;

                // S3Presigner utilise le même endpoint R2 que le S3Client
                this.s3Presigner = S3Presigner.builder()
                                .endpointOverride(URI.create(
                                                "https://" + accountId + ".r2.cloudflarestorage.com"))
                                .credentialsProvider(StaticCredentialsProvider.create(
                                                AwsBasicCredentials.create(accessKey, secretKey)))
                                .region(Region.of("auto"))
                                .build();
        }

        // ── Upload direct ─────────────────────────────────────────────────────

        @Override
        public String upload(String key, InputStream data, long size, String contentType) {
                PutObjectRequest request = PutObjectRequest.builder()
                                .bucket(bucket)
                                .key(key)
                                .contentType(contentType)
                                .contentLength(size)
                                .build();

                s3Client.putObject(request, RequestBody.fromInputStream(data, size));

                String url = buildPublicUrl(key);
                log.debug("[R2] upload réussi — key={}, url={}", key, url);
                return url;
        }

        @Override
        public void delete(String key) {
                try {
                        s3Client.deleteObject(DeleteObjectRequest.builder()
                                        .bucket(bucket)
                                        .key(key)
                                        .build());
                        log.debug("[R2] delete réussi — key={}", key);
                } catch (NoSuchKeyException e) {
                        log.debug("[R2] delete ignoré — clé inexistante : key={}", key);
                }
        }

        // ── Presigned URL ─────────────────────────────────────────────────────

        /**
         * Génère une URL pré-signée pour upload direct frontend → R2.
         *
         * <p>
         * Le frontend fait un {@code PUT} sur {@code uploadUrl} avec
         * le header {@code Content-Type} correspondant. Le fichier ne transite
         * pas par le serveur Spring.
         * </p>
         *
         * <p>
         * <b>Note sur contentLengthRange :</b> R2 supporte la contrainte de taille
         * via {@code x-amz-content-sha256} et la validation de Content-Length côté
         * client. La contrainte stricte côté serveur nécessite une Lambda@Edge ou
         * un Worker Cloudflare — pour l'app, la validation côté backend
         * (taille déclarée dans la requête JSON) est suffisante.
         * </p>
         */
        @Override
        public PresignedUploadResult genererPresignedUpload(PresignedUploadParams params) {
                PutObjectRequest putRequest = PutObjectRequest.builder()
                                .bucket(bucket)
                                .key(params.key())
                                .contentType(params.contentType())
                                .build();

                PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                                .signatureDuration(params.ttl())
                                .putObjectRequest(putRequest)
                                .build();

                PresignedPutObjectRequest presigned = s3Presigner.presignPutObject(presignRequest);

                String uploadUrl = presigned.url().toString();
                String finalPublicUrl = buildPublicUrl(params.key());
                Instant expiresAt = Instant.now().plus(params.ttl());

                log.debug("[R2] Presigned URL générée — key={}, expire={}",
                                params.key(), expiresAt);

                return new PresignedUploadResult(uploadUrl, finalPublicUrl, params.key(), expiresAt);
        }

        // ── Helpers ───────────────────────────────────────────────────────────

        private String buildPublicUrl(String key) {
                return publicUrl + "/" + key;
        }
}
