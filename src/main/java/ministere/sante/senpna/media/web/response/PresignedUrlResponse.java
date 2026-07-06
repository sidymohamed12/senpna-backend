package ministere.sante.senpna.media.web.response;

import java.time.Instant;

/**
 * Réponse retournée après génération d'une Presigned URL.
 *
 * <h3>Utilisation côté frontend :</h3>
 * 
 * <pre>
 * // Step 1 — Récupérer la Presigned URL
 * const { results } = await api.post('/api/medias/presigned-url', {
 *   mediaType: 'ACTUALITE', // ou 'PROJET', 'MEDIATHEQUE', 'FICHE_DE_POSTE', 'CV', 'LETTRE_DE_MOTIVATION'
 *   contentType: file.type,
 *   tailleBytes: file.size
 * });
 *
 * // Step 2 — Uploader DIRECTEMENT vers R2/MinIO (pas via votre serveur)
 * await fetch(results.uploadUrl, {
 *   method: 'PUT',
 *   headers: { 'Content-Type': file.type },
 *   body: file
 * });
 *
 * // Step 3 — Créer l'actualité avec l'URL finale
 * await api.post('/api/actualites', {
 *   nom: 'Thiéboudienne',
 *   imageUrl: results.publicUrl,   // ← URL permanente à stocker
 *   ...
 * });
 * </pre>
 *
 * @param uploadUrl URL signée vers laquelle faire le PUT (expire après
 *                  {@code expiresAt})
 * @param publicUrl URL publique permanente de l'image (à passer dans le POST
 *                  actualite)
 * @param key       clé de stockage (utile pour logs / debug)
 * @param expiresAt timestamp ISO-8601 d'expiration de l'URL d'upload
 */
public record PresignedUrlResponse(
                String uploadUrl,
                String publicUrl,
                String key,
                Instant expiresAt) {
}
