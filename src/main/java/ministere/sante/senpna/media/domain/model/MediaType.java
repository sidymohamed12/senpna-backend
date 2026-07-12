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
            Set.of(
                    MimeTypes.IMAGE_JPEG,
                    MimeTypes.IMAGE_PNG,
                    MimeTypes.IMAGE_WEBP,
                    MimeTypes.IMAGE_GIF,
                    MimeTypes.VIDEO_MP4),
            50 * 1024 * 1024L,
            "actualites/"),

    PROJET(
            Set.of(
                    MimeTypes.IMAGE_JPEG,
                    MimeTypes.IMAGE_PNG,
                    MimeTypes.IMAGE_WEBP),
            5 * 1024 * 1024L,
            "projets/"),

    MEDIATHEQUE(
            Set.of(
                    MimeTypes.IMAGE_JPEG,
                    MimeTypes.IMAGE_PNG,
                    MimeTypes.IMAGE_WEBP,
                    MimeTypes.IMAGE_GIF,
                    MimeTypes.VIDEO_MP4),
            50 * 1024 * 1024L,
            "mediatheque/"),

    FICHE_DE_POSTE(
            Set.of(
                    MimeTypes.APPLICATION_PDF,
                    MimeTypes.IMAGE_JPEG,
                    MimeTypes.IMAGE_PNG,
                    MimeTypes.IMAGE_WEBP),
            5 * 1024 * 1024L,
            "fiches-de-poste/"),

    CV(
            Set.of(
                    MimeTypes.APPLICATION_PDF,
                    MimeTypes.APPLICATION_MSWORD,
                    MimeTypes.APPLICATION_DOCX),
            5 * 1024 * 1024L,
            "cvs/",
            true),

    LETTRE_DE_MOTIVATION(
            Set.of(
                    MimeTypes.APPLICATION_PDF,
                    MimeTypes.APPLICATION_MSWORD,
                    MimeTypes.APPLICATION_DOCX),
            5 * 1024 * 1024L,
            "lettres-de-motivation/",
            true);

    private final Set<String> contentTypesAutorises;
    private final long maxSizeBytes;
    private final String prefixe;
    private final boolean utilisablePubliquement;

    MediaType(Set<String> contentTypesAutorises, long maxSizeBytes, String prefixe) {
        this(contentTypesAutorises, maxSizeBytes, prefixe, false);
    }

    MediaType(
            Set<String> contentTypesAutorises,
            long maxSizeBytes,
            String prefixe,
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
     * restent accessibles uniquement via l'endpoint authentifié.
     */
    public boolean estUtilisablePubliquement() {
        return utilisablePubliquement;
    }
}
