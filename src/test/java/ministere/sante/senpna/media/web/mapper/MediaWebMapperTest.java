package ministere.sante.senpna.media.web.mapper;

import ministere.sante.senpna.media.application.dto.GenererPresignedUrlCommand;
import ministere.sante.senpna.media.application.dto.PresignedUrlResult;
import ministere.sante.senpna.media.domain.model.MediaType;
import ministere.sante.senpna.media.domain.model.MimeTypes;
import ministere.sante.senpna.media.web.request.PresignedUrlRequest;
import ministere.sante.senpna.media.web.response.PresignedUrlResponse;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("MediaWebMapper — mapping requêtes/réponses HTTP du module media")
class MediaWebMapperTest {

    MediaWebMapper sut = new MediaWebMapper();

    @Test
    @DisplayName("versCommand() reporte fidèlement chaque champ de la requête")
    void versCommand_reporteChaqueChamp() {
        PresignedUrlRequest request = new PresignedUrlRequest(MediaType.CV, MimeTypes.APPLICATION_PDF, 500_000L);

        GenererPresignedUrlCommand command = sut.versCommand(request);

        assertThat(command.mediaType()).isEqualTo(MediaType.CV);
        assertThat(command.contentType()).isEqualTo(MimeTypes.APPLICATION_PDF);
        assertThat(command.tailleBytes()).isEqualTo(500_000L);
    }

    @Test
    @DisplayName("versResponse() reporte fidèlement chaque champ du résultat")
    void versResponse_reporteChaqueChamp() {
        Instant expiration = Instant.now().plusSeconds(600);
        PresignedUrlResult result = new PresignedUrlResult("https://upload", "https://public", "cvs/x.pdf",
                expiration);

        PresignedUrlResponse response = sut.versResponse(result);

        assertThat(response.uploadUrl()).isEqualTo("https://upload");
        assertThat(response.publicUrl()).isEqualTo("https://public");
        assertThat(response.key()).isEqualTo("cvs/x.pdf");
        assertThat(response.expiresAt()).isEqualTo(expiration);
    }
}
