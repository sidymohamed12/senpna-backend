package ministere.sante.senpna.auth.application.service;

import ministere.sante.senpna.auth.domain.valueobject.OtpChannel;
import ministere.sante.senpna.auth.fixtures.UserFixtures;
import ministere.sante.senpna.shared.domain.exception.SenPnaException;
import ministere.sante.senpna.shared.domain.model.User;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("OtpDestinationResolver — résolution de la destination d'envoi OTP")
class OtpDestinationResolverTest {

    OtpDestinationResolver sut = new OtpDestinationResolver();

    @Test
    @DisplayName("canal EMAIL → adresse e-mail de l'utilisateur")
    void canalEmail_adresseEmail() {
        User user = UserFixtures.actif();

        assertThat(sut.resoudre(user, OtpChannel.EMAIL)).isEqualTo(UserFixtures.EMAIL);
    }

    @Test
    @DisplayName("canal SMS avec téléphone enregistré → numéro de téléphone")
    void canalSms_avecTelephone() {
        User user = UserFixtures.actifAvecTelephone();

        assertThat(sut.resoudre(user, OtpChannel.SMS)).isEqualTo(UserFixtures.TELEPHONE);
    }

    @Test
    @DisplayName("canal SMS sans téléphone enregistré → BusinessRuleException")
    void canalSms_sansTelephone_leveException() {
        User user = UserFixtures.actif();

        assertThatThrownBy(() -> sut.resoudre(user, OtpChannel.SMS))
                .isInstanceOf(SenPnaException.class)
                .hasMessageContaining("e-mail");
    }
}
