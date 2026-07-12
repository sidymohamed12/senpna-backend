package ministere.sante.senpna.media.domain.model;

public final class MimeTypes {

    private MimeTypes() {
        throw new IllegalStateException("Utility class");
    }

    // Images
    public static final String IMAGE_JPEG = "image/jpeg";
    public static final String IMAGE_PNG = "image/png";
    public static final String IMAGE_WEBP = "image/webp";
    public static final String IMAGE_GIF = "image/gif";

    // Vidéos
    public static final String VIDEO_MP4 = "video/mp4";

    // Documents
    public static final String APPLICATION_PDF = "application/pdf";
    public static final String APPLICATION_MSWORD = "application/msword";
    public static final String APPLICATION_DOCX = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
}