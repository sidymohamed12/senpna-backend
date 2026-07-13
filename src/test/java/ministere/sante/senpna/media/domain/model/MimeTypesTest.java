package ministere.sante.senpna.media.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("MimeTypes — constantes de content-types MIME")
class MimeTypesTest {

    @Test
    @DisplayName("les constantes exposent les valeurs MIME attendues")
    void constantes_valeursAttendues() {
        assertThat(MimeTypes.IMAGE_JPEG).isEqualTo("image/jpeg");
        assertThat(MimeTypes.IMAGE_PNG).isEqualTo("image/png");
        assertThat(MimeTypes.IMAGE_WEBP).isEqualTo("image/webp");
        assertThat(MimeTypes.IMAGE_GIF).isEqualTo("image/gif");
        assertThat(MimeTypes.VIDEO_MP4).isEqualTo("video/mp4");
        assertThat(MimeTypes.APPLICATION_PDF).isEqualTo("application/pdf");
        assertThat(MimeTypes.APPLICATION_MSWORD).isEqualTo("application/msword");
        assertThat(MimeTypes.APPLICATION_DOCX)
                .isEqualTo("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
    }

    @Test
    @DisplayName("classe utilitaire — constructeur privé et non instanciable")
    void constructeur_priveEtNonInstanciable() throws Exception {
        Constructor<MimeTypes> constructor = MimeTypes.class.getDeclaredConstructor();
        assertThat(Modifier.isPrivate(constructor.getModifiers())).isTrue();

        constructor.setAccessible(true);

        assertThatThrownBy(() -> invokeConstructor(constructor))
                .isInstanceOf(IllegalStateException.class);
    }

    private static void invokeConstructor(Constructor<MimeTypes> constructor) throws Throwable {
        try {
            constructor.newInstance();
        } catch (InvocationTargetException e) {
            throw e.getCause();
        }
    }
}
