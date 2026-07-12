package ministere.sante.senpna.media.infrastructure.stockage;

import ministere.sante.senpna.media.domain.port.out.StoragePort.PresignedUploadParams;
import ministere.sante.senpna.media.domain.port.out.StoragePort.PresignedUploadResult;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("MockStorageAdapter — bouchon de stockage objet (profil test)")
class MockStorageAdapterTest {

    MockStorageAdapter sut;

    @BeforeEach
    void setUp() {
        sut = new MockStorageAdapter();
    }

    @Nested
    @DisplayName("upload() / delete()")
    class UploadEtDelete {

        @Test
        @DisplayName("upload() enregistre la clé et renvoie une URL publique mock cohérente")
        void upload_enregistreLaCle() {
            String url = sut.upload("actualites/photo.jpg", new ByteArrayInputStream(new byte[0]), 100,
                    "image/jpeg");

            assertThat(url).contains("actualites/photo.jpg");
            assertThat(sut.wasUploaded("actualites/photo.jpg")).isTrue();
            assertThat(sut.getKeys()).containsExactly("actualites/photo.jpg");
        }

        @Test
        @DisplayName("delete() retire la clé du stockage simulé")
        void delete_retireLaCle() {
            sut.upload("k1", new ByteArrayInputStream(new byte[0]), 10, "image/png");

            sut.delete("k1");

            assertThat(sut.wasUploaded("k1")).isFalse();
        }

        @Test
        @DisplayName("delete() sur une clé inexistante est idempotent, ne lève pas d'exception")
        void delete_cleInexistante_idempotent() {
            org.assertj.core.api.Assertions.assertThatCode(() -> sut.delete("inexistante"))
                    .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("genererPresignedUpload()")
    class GenererPresignedUpload {

        @Test
        @DisplayName("génère une URL d'upload et une URL publique cohérentes avec la clé demandée")
        void genereUrlsCoherentes() {
            PresignedUploadParams params = new PresignedUploadParams("cvs/abc.pdf", "application/pdf", 5_000_000L,
                    Duration.ofMinutes(10));

            PresignedUploadResult result = sut.genererPresignedUpload(params);

            assertThat(result.key()).isEqualTo("cvs/abc.pdf");
            assertThat(result.uploadUrl()).contains("cvs/abc.pdf");
            assertThat(result.publicUrl()).contains("cvs/abc.pdf");
            assertThat(result.expiresAt()).isAfter(Instant.now());
        }

        @Test
        @DisplayName("pré-enregistre la clé comme uploadée, avant même le PUT réel")
        void preEnregistreLaCle() {
            PresignedUploadParams params = new PresignedUploadParams("k", "image/png", 1000L,
                    Duration.ofMinutes(5));

            sut.genererPresignedUpload(params);

            assertThat(sut.wasUploaded("k")).isTrue();
        }

        @Test
        @DisplayName("chaque appel incrémente le compteur et alimente l'historique")
        void incrementeCompteurEtHistorique() {
            sut.genererPresignedUpload(new PresignedUploadParams("k1", "image/png", 1000L, Duration.ofMinutes(5)));
            sut.genererPresignedUpload(new PresignedUploadParams("k2", "image/png", 1000L, Duration.ofMinutes(5)));

            assertThat(sut.getNombrePresignedUrls()).isEqualTo(2);
            assertThat(sut.getGeneratedPresignedUrls()).extracting(PresignedUploadResult::key)
                    .containsExactly("k1", "k2");
            assertThat(sut.getDernierePresignedUrl().key()).isEqualTo("k2");
        }

        @Test
        @DisplayName("aucune Presigned URL générée → getDernierePresignedUrl() renvoie null")
        void aucunePresignedUrl_derniereEstNull() {
            assertThat(sut.getDernierePresignedUrl()).isNull();
            assertThat(sut.getNombrePresignedUrls()).isZero();
        }
    }

    @Nested
    @DisplayName("reset()")
    class Reset {

        @Test
        @DisplayName("remet le mock dans son état initial")
        void remetEtatInitial() {
            sut.upload("k1", new ByteArrayInputStream(new byte[0]), 10, "image/png");
            sut.genererPresignedUpload(new PresignedUploadParams("k2", "image/png", 1000L, Duration.ofMinutes(5)));

            sut.reset();

            assertThat(sut.getKeys()).isEmpty();
            assertThat(sut.getNombrePresignedUrls()).isZero();
            assertThat(sut.getDernierePresignedUrl()).isNull();
        }
    }
}
