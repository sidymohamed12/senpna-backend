package ministere.sante.senpna.media.web.mapper;

import ministere.sante.senpna.media.application.dto.GenererPresignedUrlCommand;
import ministere.sante.senpna.media.application.dto.PresignedUrlResult;
import ministere.sante.senpna.media.web.request.PresignedUrlRequest;
import ministere.sante.senpna.media.web.response.PresignedUrlResponse;
import org.springframework.stereotype.Component;

@Component
public class MediaWebMapper {

    public GenererPresignedUrlCommand versCommand(PresignedUrlRequest request) {
        return new GenererPresignedUrlCommand(
                request.mediaType(),
                request.contentType(),
                request.tailleBytes());
    }

    public PresignedUrlResponse versResponse(PresignedUrlResult result) {
        return new PresignedUrlResponse(
                result.uploadUrl(),
                result.publicUrl(),
                result.key(),
                result.expiresAt());
    }
}
