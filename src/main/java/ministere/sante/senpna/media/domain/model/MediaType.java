package ministere.sante.senpna.media.domain.model;

import java.util.Set;

/**
 * Énumération des types de médias supportés pour l'upload.
 *
 * <p>
 * Chaque type définit :
 * <ul>
 * <li>Les Content-Types MIME autorisés</li>
 * <li>La taille maximale du fichier</li>
 * <li>Le préfixe de clé dans le bucket</li>
 * </ul>
 * </p>
 */
public enum MediaType {

    ACTUALITE(
            Set.of("image/jpeg", "image/png", "image/webp", "image/gif", "video/mp4"),
            50 * 1024 * 1024L, // 50MB
            "actualites/"),

    PROJET(
            Set.of("image/jpeg", "image/png", "image/webp"),
            5 * 1024 * 1024L, // 5MB
            "projets/"),

    MEDIATHEQUE(
            Set.of("image/jpeg", "image/png", "image/webp", "image/gif", "video/mp4"),
            50 * 1024 * 1024L, // 50MB
            "mediatheque/"),

    FICHE_DE_POSTE(
            Set.of("application/pdf", "image/jpeg", "image/png", "image/webp"),
            5 * 1024 * 1024L, // 5MB
            "fiches-de-poste/"),

    CV(
            Set.of(
                    "application/pdf",
                    "application/msword",
                    "application/vnd.openxmlformats-officedocument.wordprocessingml.document"),
            5 * 1024 * 1024L, // 5MB
            "cvs/",
            true),

    LETTRE_DE_MOTIVATION(
            Set.of(
                    "application/pdf",
                    "application/msword",
                    "application/vnd.openxmlformats-officedocument.wordprocessingml.document"),
            5 * 1024 * 1024L, // 5MB
            "lettres-de-motivation/",
            true);

    private final Set<String> contentTypesAutorises;
    private final long maxSizeBytes;
    private final String prefixe;
    private final boolean utilisablePubliquement;

    MediaType(Set<String> contentTypesAutorises, long maxSizeBytes, String prefixe) {
        this(contentTypesAutorises, maxSizeBytes, prefixe, false);
    }

    MediaType(Set<String> contentTypesAutorises, long maxSizeBytes, String prefixe,
            boolean utilisablePubliquement) {
        this.contentTypesAutorises = contentTypesAutorises;
        this.maxSizeBytes = maxSizeBytes;
        this.prefixe = prefixe;
        this.utilisablePubliquement = utilisablePubliquement;
    }

    public Set<String> getContentTypesAutorises() {
        return contentTypesAutorises;
    }

    public long getMaxSizeBytes() {
        return maxSizeBytes;
    }

    public String getPrefixe() {
        return prefixe;
    }

    public boolean accepteContentType(String contentType) {
        return contentType != null && contentTypesAutorises.contains(contentType);
    }

    public String getMaxSizeEnMo() {
        return (maxSizeBytes / (1024 * 1024)) + "MB";
    }

    /**
     * {@code true} pour les types de médias pouvant être demandés sans
     * authentification via {@code POST /api/medias/presigned-url/public}
     * — strictement limité aux pièces jointes d'un formulaire public
     * (candidature : CV, lettre de motivation). Tous les autres types
     * (contenus éditoriaux gérés par du personnel authentifié) restent
     * exclusivement accessibles via l'endpoint authentifié standard.
     */
    public boolean estUtilisablePubliquement() {
        return utilisablePubliquement;
    }
}
