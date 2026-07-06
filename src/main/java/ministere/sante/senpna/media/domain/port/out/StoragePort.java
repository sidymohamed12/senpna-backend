package ministere.sante.senpna.media.domain.port.out;

import java.io.InputStream;
import java.time.Duration;
import java.time.Instant;

/**
 * Port de sortie — stockage objet (ADR-004, ADR-011).
 *
 * <h3>Phase 1 — upload serveur</h3>
 * <p>
 * Méthodes {@code upload()} et {@code delete()} — utilisées par les
 * adaptateurs directs (MinIO dev, R2 prod, Mock test).
 * </p>
 *
 * <h3>Phase 2 — Presigned URL (ADR-004 étendu)</h3>
 * <p>
 * Méthode {@code genererPresignedUpload()} — le fichier ne transite plus
 * par le serveur Spring. Le frontend uploade directement vers R2/MinIO.
 * Le serveur ne gère qu'une petite requête JSON pour générer l'URL signée,
 * puis une autre pour créer l'actualité avec l'URL finale.
 * </p>
 *
 * <pre>
 * Flux Presigned URL :
 *   1. POST /api/medias/presigned-url  → ~2ms (génération URL signée)
 *   2. PUT  {presignedUrl}             → frontend → R2 directement
 *   3. POST /api/actualites              → ~2ms (JSON pur, imageUrl fournie)
 *
 * Avantages vs endpoint upload direct :
 *   - Zéro RAM serveur consommée par le fichier
 *   - Zéro bande passante serveur utilisée
 *   - Thread Tomcat libéré immédiatement après génération de l'URL
 * </pre>
 *
 * <h3>Implémentations :</h3>
 * 
 * <pre>
 *   Profil "dev"  → MinioStorageAdapter  (shared/infrastructure/storage/)
 *   Profil "prod" → R2StorageAdapter     (shared/infrastructure/storage/)
 *   Profil "test" → MockStorageAdapter   (shared/infrastructure/storage/)
 * </pre>
 */
public interface StoragePort {

    // ── Upload direct (conservé pour compatibilité interne) ───────────────

    /**
     * Upload un fichier et retourne son URL publique stable.
     *
     * @param key         clé unique du fichier dans le bucket
     * @param data        flux de données du fichier
     * @param size        taille en octets
     * @param contentType type MIME (ex: {@code "image/jpeg"})
     * @return URL publique du fichier uploadé
     */
    String upload(String key, InputStream data, long size, String contentType);

    /**
     * Supprime un fichier du stockage.
     * Opération idempotente — ne lève pas d'exception si la clé n'existe pas.
     *
     * @param key clé du fichier à supprimer
     */
    void delete(String key);

    // ── Presigned URL (upload direct frontend → stockage) ─────────────────

    /**
     * Génère une URL pré-signée permettant au frontend d'uploader
     * directement vers le stockage, sans transiter par le serveur Spring.
     *
     * <p>
     * L'URL générée est valide pendant {@link PresignedUploadParams#ttl()}
     * et contrainte au {@link PresignedUploadParams#contentType()} et à la
     * taille maximale {@link PresignedUploadParams#maxSizeBytes()} déclarés.
     * </p>
     *
     * @param params paramètres de génération
     * @return URL signée + URL publique finale de l'image
     */
    PresignedUploadResult genererPresignedUpload(PresignedUploadParams params);

    // ── Records ───────────────────────────────────────────────────────────

    /**
     * Paramètres de génération d'une Presigned URL.
     *
     * @param key          clé cible dans le bucket (ex: "medias/uuid.jpg")
     * @param contentType  type MIME autorisé pour cet upload
     * @param maxSizeBytes taille maximale du fichier en octets
     * @param ttl          durée de validité de l'URL signée
     */
    record PresignedUploadParams(
            String key,
            String contentType,
            long maxSizeBytes,
            Duration ttl) {
        public PresignedUploadParams {
            if (key == null || key.isBlank())
                throw new IllegalArgumentException("La clé ne peut pas être vide");
            if (contentType == null || contentType.isBlank())
                throw new IllegalArgumentException("Le contentType ne peut pas être vide");
            if (maxSizeBytes <= 0)
                throw new IllegalArgumentException("maxSizeBytes doit être positif");
            if (ttl == null || ttl.isNegative() || ttl.isZero())
                throw new IllegalArgumentException("Le TTL doit être positif");
        }
    }

    /**
     * Résultat de la génération d'une Presigned URL.
     *
     * @param uploadUrl URL signée vers laquelle le frontend fait un PUT
     *                  (expire après {@code expiresAt})
     * @param publicUrl URL publique finale de l'image, à stocker en base
     *                  (permanente, indépendante de l'expiration de l'upload)
     * @param key       clé dans le bucket (utile pour suppression future)
     * @param expiresAt timestamp d'expiration de l'URL d'upload
     */
    record PresignedUploadResult(
            String uploadUrl,
            String publicUrl,
            String key,
            Instant expiresAt) {
    }
}
