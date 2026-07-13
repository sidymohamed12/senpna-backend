package ministere.sante.senpna.carriere.infrastructure.mail;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;

@DisplayName("LoggingCandidatureMailAdapter — bouchon de mail (profil test)")
class LoggingCandidatureMailAdapterTest {

    LoggingCandidatureMailAdapter sut = new LoggingCandidatureMailAdapter();

    @Test
    @DisplayName("envoyerAccuseReceptionCandidature() journalise sans lever d'exception")
    void accuseReception_neLeveRien() {
        assertThatCode(() -> sut.envoyerAccuseReceptionCandidature("c@mail.sn", "Ibra", "Dev", "Entreprise"))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("envoyerNotificationNouvelleCandidature() journalise sans lever d'exception")
    void notificationRH_neLeveRien() {
        assertThatCode(() -> sut.envoyerNotificationNouvelleCandidature("rh@mail.sn", "Dev", "Entreprise", "Ibra",
                "c@mail.sn", "+221771234567")).doesNotThrowAnyException();
    }
}
