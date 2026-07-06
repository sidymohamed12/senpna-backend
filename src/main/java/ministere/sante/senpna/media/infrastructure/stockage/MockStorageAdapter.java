package ministere.sante.senpna.media.infrastructure.stockage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ministere.sante.senpna.media.domain.port.out.StoragePort;

import java.io.InputStream;
import java.time.Instant;
import java.util.*;

/**
 * Adaptateur de stockage bouchon — profil "test" uniquement.
 *
 * <p>
 * Simule upload et génération de Presigned URL en mémoire,
 * sans aucune connexion réseau.
 * </p>
 */
public class MockStorageAdapter implements StoragePort {

    private static final Logger log = LoggerFactory.getLogger(MockStorageAdapter.class);
    private static final String MOCK_BASE_URL = "mock://resto-facile-media/";
    private static final String MOCK_PRESIGN_URL = "mock://presign/";

    /** Fichiers uploadés directement (upload()) */
    private final Map<String, String> storedKeys = Collections.synchronizedMap(new LinkedHashMap<>());

    /** Presigned URLs générées (genererPresignedUpload()) */
    private final List<PresignedUploadResult> generatedPresignedUrls = Collections.synchronizedList(new ArrayList<>());

    // ── Upload direct ─────────────────────────────────────────────────────

    @Override
    public String upload(String key, InputStream data, long size, String contentType) {
        String url = MOCK_BASE_URL + key;
        storedKeys.put(key, url);
        log.debug("[MockStorage] upload simulé — key={}, size={}", key, size);
        return url;
    }

    @Override
    public void delete(String key) {
        storedKeys.remove(key);
        log.debug("[MockStorage] delete simulé — key={}", key);
    }

    // ── Presigned URL ─────────────────────────────────────────────────────

    /**
     * Génère une Presigned URL simulée pour les tests.
     * L'URL d'upload est fictive, l'URL publique est cohérente avec
     * celle retournée par {@link #upload(String, InputStream, long, String)}.
     */
    @Override
    public PresignedUploadResult genererPresignedUpload(PresignedUploadParams params) {
        String uploadUrl = MOCK_PRESIGN_URL + params.key()
                + "?X-Mock-Signature=test&X-Expires=" + params.ttl().getSeconds();
        String finalPublicUrl = MOCK_BASE_URL + params.key();
        Instant expiresAt = Instant.now().plus(params.ttl());

        PresignedUploadResult result = new PresignedUploadResult(
                uploadUrl, finalPublicUrl, params.key(), expiresAt);

        generatedPresignedUrls.add(result);

        // Pré-enregistrer la clé comme si le fichier allait être uploadé
        // (utile pour les tests qui vérifient l'URL finale avant le PUT réel)
        storedKeys.put(params.key(), finalPublicUrl);

        log.debug("[MockStorage] Presigned URL simulée — key={}", params.key());
        return result;
    }

    // ── Méthodes d'inspection pour les tests ──────────────────────────────

    /** Vue non modifiable des clés uploadées → URLs. */
    public Map<String, String> getStoredKeys() {
        return Collections.unmodifiableMap(storedKeys);
    }

    /** Clés uploadées uniquement. */
    public Set<String> getKeys() {
        return Collections.unmodifiableSet(storedKeys.keySet());
    }

    /** Vérifie qu'une clé a bien été uploadée ou pré-signée. */
    public boolean wasUploaded(String key) {
        return storedKeys.containsKey(key);
    }

    /** Dernière Presigned URL générée, ou null si aucune. */
    public PresignedUploadResult getDernierePresignedUrl() {
        return generatedPresignedUrls.isEmpty() ? null
                : generatedPresignedUrls.get(generatedPresignedUrls.size() - 1);
    }

    /** Nombre de Presigned URLs générées depuis le dernier reset(). */
    public int getNombrePresignedUrls() {
        return generatedPresignedUrls.size();
    }

    /** Toutes les Presigned URLs générées. */
    public List<PresignedUploadResult> getGeneratedPresignedUrls() {
        return Collections.unmodifiableList(generatedPresignedUrls);
    }

    /** Remet le mock dans son état initial — appeler dans {@code @BeforeEach}. */
    public void reset() {
        storedKeys.clear();
        generatedPresignedUrls.clear();
    }
}
