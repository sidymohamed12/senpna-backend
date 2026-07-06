package ministere.sante.senpna.media.application.dto;

import ministere.sante.senpna.media.domain.model.MediaType;

public record GenererPresignedUrlCommand(
        MediaType mediaType,
        String contentType,
        long tailleBytes) {
    public GenererPresignedUrlCommand {
        if (mediaType == null)
            throw new IllegalArgumentException("Le type de média est obligatoire");
        if (contentType == null || contentType.isBlank())
            throw new IllegalArgumentException("Le contentType est obligatoire");
        if (tailleBytes <= 0)
            throw new IllegalArgumentException("La taille doit être positive");
    }
}
