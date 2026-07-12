package ministere.sante.senpna.media.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("MediaType — types de médias supportés pour l'upload")
class MediaTypeTest {

    @Nested
    @DisplayName("accepteContentType()")
    class AccepteContentType {

        @Test
        @DisplayName("content-type autorisé pour le type → true")
        void contentTypeAutorise() {
            assertThat(MediaType.ACTUALITE.accepteContentType(MimeTypes.IMAGE_PNG)).isTrue();
        }

        @Test
        @DisplayName("content-type non autorisé pour le type → false")
        void contentTypeNonAutorise() {
            assertThat(MediaType.PROJET.accepteContentType(MimeTypes.VIDEO_MP4)).isFalse();
        }

        @Test
        @DisplayName("content-type null → false")
        void contentTypeNull() {
            assertThat(MediaType.ACTUALITE.accepteContentType(null)).isFalse();
        }
    }

    @Nested
    @DisplayName("getMaxSizeEnMo()")
    class GetMaxSizeEnMo {

        @Test
        @DisplayName("PROJET (5 Mo) → \"5MB\"")
        void projet() {
            assertThat(MediaType.PROJET.getMaxSizeEnMo()).isEqualTo("5MB");
        }

        @Test
        @DisplayName("ACTUALITE (50 Mo) → \"50MB\"")
        void actualite() {
            assertThat(MediaType.ACTUALITE.getMaxSizeEnMo()).isEqualTo("50MB");
        }
    }

    @Nested
    @DisplayName("estUtilisablePubliquement()")
    class EstUtilisablePubliquement {

        @Test
        @DisplayName("CV et LETTRE_DE_MOTIVATION sont utilisables publiquement")
        void cvEtLettreMotivationPublics() {
            assertThat(MediaType.CV.estUtilisablePubliquement()).isTrue();
            assertThat(MediaType.LETTRE_DE_MOTIVATION.estUtilisablePubliquement()).isTrue();
        }

        @Test
        @DisplayName("les autres types ne sont pas utilisables publiquement par défaut")
        void autresTypesPrives() {
            assertThat(MediaType.ACTUALITE.estUtilisablePubliquement()).isFalse();
            assertThat(MediaType.PROJET.estUtilisablePubliquement()).isFalse();
            assertThat(MediaType.MEDIATHEQUE.estUtilisablePubliquement()).isFalse();
            assertThat(MediaType.FICHE_DE_POSTE.estUtilisablePubliquement()).isFalse();
        }
    }

    @Nested
    @DisplayName("getPrefixe() / getContentTypesAutorises() / getMaxSizeBytes()")
    class Accesseurs {

        @Test
        @DisplayName("chaque type expose son préfixe de clé de bucket")
        void prefixe() {
            assertThat(MediaType.CV.getPrefixe()).isEqualTo("cvs/");
            assertThat(MediaType.MEDIATHEQUE.getPrefixe()).isEqualTo("mediatheque/");
        }

        @Test
        @DisplayName("les content-types autorisés pour CV incluent PDF et Word")
        void contentTypesAutorisesCv() {
            assertThat(MediaType.CV.getContentTypesAutorises())
                    .containsExactlyInAnyOrder(
                            MimeTypes.APPLICATION_PDF,
                            MimeTypes.APPLICATION_MSWORD,
                            MimeTypes.APPLICATION_DOCX);
        }

        @Test
        @DisplayName("la taille maximale en octets correspond à la valeur configurée")
        void maxSizeBytes() {
            assertThat(MediaType.PROJET.getMaxSizeBytes()).isEqualTo(5 * 1024 * 1024L);
        }

        @ParameterizedTest
        @EnumSource(MediaType.class)
        @DisplayName("chaque type a un préfixe et au moins un content-type autorisé")
        void toutTypeEstCorrectementConfigure(MediaType type) {
            assertThat(type.getPrefixe()).isNotBlank();
            assertThat(type.getContentTypesAutorises()).isNotEmpty();
            assertThat(type.getMaxSizeBytes()).isPositive();
        }
    }
}
