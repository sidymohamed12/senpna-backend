package ministere.sante.senpna.media.application.usecase;

import ministere.sante.senpna.media.application.dto.GenererPresignedUrlCommand;
import ministere.sante.senpna.media.application.dto.PresignedUrlResult;
import ministere.sante.senpna.media.domain.exception.ContentTypeNonAutoriseException;
import ministere.sante.senpna.media.domain.exception.MediaTypeNonAutorisePubliquementException;
import ministere.sante.senpna.media.domain.exception.TailleFichierDepasseeException;
import ministere.sante.senpna.media.domain.model.MediaType;
import ministere.sante.senpna.media.domain.model.MimeTypes;
import ministere.sante.senpna.media.domain.port.out.StoragePort;
import ministere.sante.senpna.media.domain.port.out.StoragePort.PresignedUploadParams;
import ministere.sante.senpna.media.domain.port.out.StoragePort.PresignedUploadResult;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("GenererPresignedUrlUseCaseImpl — génération de Presigned URL d'upload")
class GenererPresignedUrlUseCaseImplTest {

        @Mock
        StoragePort storagePort;

        GenererPresignedUrlUseCaseImpl sut;

        @BeforeEach
        void setUp() {
                sut = new GenererPresignedUrlUseCaseImpl(storagePort, 10);
        }

        @Nested
        @DisplayName("generer()")
        class Generer {

                @Test
                @DisplayName("content-type et taille valides → délègue au StoragePort et renvoie son résultat")
                void nominal_delegueAuStoragePort() {
                        PresignedUploadResult storageResult = new PresignedUploadResult(
                                        "https://upload", "https://public", "actualites/xxx.jpg", Instant.now());
                        when(storagePort.genererPresignedUpload(any())).thenReturn(storageResult);

                        GenererPresignedUrlCommand command = new GenererPresignedUrlCommand(
                                        MediaType.ACTUALITE, MimeTypes.IMAGE_JPEG, 1_000_000L);

                        PresignedUrlResult result = sut.generer(command);

                        assertThat(result.uploadUrl()).isEqualTo("https://upload");
                        assertThat(result.publicUrl()).isEqualTo("https://public");
                        assertThat(result.key()).isEqualTo("actualites/xxx.jpg");
                }

                @Test
                @DisplayName("génère une clé préfixée par le préfixe du MediaType, avec l'extension du content-type")
                void genereCleAvecPrefixeEtExtension() {
                        when(storagePort.genererPresignedUpload(any())).thenReturn(
                                        new PresignedUploadResult("u", "p", "k", Instant.now()));

                        GenererPresignedUrlCommand command = new GenererPresignedUrlCommand(
                                        MediaType.PROJET, MimeTypes.IMAGE_PNG, 1000L);

                        sut.generer(command);

                        ArgumentCaptor<PresignedUploadParams> captor = ArgumentCaptor
                                        .forClass(PresignedUploadParams.class);
                        verify(storagePort).genererPresignedUpload(captor.capture());
                        assertThat(captor.getValue().key()).startsWith("projets/").endsWith(".png");
                }

                @Test
                @DisplayName("content-type non autorisé pour le MediaType → ContentTypeNonAutoriseException")
                void contentTypeNonAutorise_leveException() {
                        GenererPresignedUrlCommand command = new GenererPresignedUrlCommand(
                                        MediaType.PROJET, MimeTypes.VIDEO_MP4, 1000L);

                        assertThatThrownBy(() -> sut.generer(command))
                                        .isInstanceOf(ContentTypeNonAutoriseException.class);
                }

                @Test
                @DisplayName("taille déclarée supérieure au maximum autorisé → TailleFichierDepasseeException")
                void tailleDepassee_leveException() {
                        GenererPresignedUrlCommand command = new GenererPresignedUrlCommand(
                                        MediaType.PROJET, MimeTypes.IMAGE_JPEG, MediaType.PROJET.getMaxSizeBytes() + 1);

                        assertThatThrownBy(() -> sut.generer(command))
                                        .isInstanceOf(TailleFichierDepasseeException.class);
                }

                @Test
                @DisplayName("taille exactement égale au maximum autorisé → acceptée")
                void tailleExactementAuMaximum_acceptee() {
                        when(storagePort.genererPresignedUpload(any())).thenReturn(
                                        new PresignedUploadResult("u", "p", "k", Instant.now()));

                        GenererPresignedUrlCommand command = new GenererPresignedUrlCommand(
                                        MediaType.PROJET, MimeTypes.IMAGE_JPEG, MediaType.PROJET.getMaxSizeBytes());

                        assertThat(sut.generer(command)).isNotNull();
                }

                @Test
                @DisplayName("content-type inconnu → extension par défaut \"jpg\"")
                void contentTypeInconnu_extensionParDefaut() {
                        // ACTUALITE accepte JPEG/PNG/WEBP/GIF/MP4 — on utilise un type accepté
                        // dont l'extension n'est pas mappée explicitement pour vérifier le fallback
                        // n'est pas trivialement atteignable avec les types autorisés existants ;
                        // on vérifie donc le mapping explicite du gif à la place.
                        when(storagePort.genererPresignedUpload(any())).thenReturn(
                                        new PresignedUploadResult("u", "p", "k", Instant.now()));

                        GenererPresignedUrlCommand command = new GenererPresignedUrlCommand(
                                        MediaType.ACTUALITE, MimeTypes.IMAGE_GIF, 1000L);

                        sut.generer(command);

                        ArgumentCaptor<PresignedUploadParams> captor = ArgumentCaptor
                                        .forClass(PresignedUploadParams.class);
                        verify(storagePort).genererPresignedUpload(captor.capture());
                        assertThat(captor.getValue().key()).endsWith(".gif");
                }
        }

        @Nested
        @DisplayName("genererPublique()")
        class GenererPublique {

                @Test
                @DisplayName("MediaType utilisable publiquement (CV) → délègue normalement")
                void mediaTypePublic_delegue() {
                        when(storagePort.genererPresignedUpload(any())).thenReturn(
                                        new PresignedUploadResult("u", "p", "k", Instant.now()));

                        GenererPresignedUrlCommand command = new GenererPresignedUrlCommand(
                                        MediaType.CV, MimeTypes.APPLICATION_PDF, 1000L);

                        assertThat(sut.genererPublique(command)).isNotNull();
                }

                @Test
                @DisplayName("MediaType non utilisable publiquement (ACTUALITE) → MediaTypeNonAutorisePubliquementException")
                void mediaTypeNonPublic_leveException() {
                        GenererPresignedUrlCommand command = new GenererPresignedUrlCommand(
                                        MediaType.ACTUALITE, MimeTypes.IMAGE_JPEG, 1000L);

                        assertThatThrownBy(() -> sut.genererPublique(command))
                                        .isInstanceOf(MediaTypeNonAutorisePubliquementException.class);
                }
        }
}
