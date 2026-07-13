package ministere.sante.senpna.auth.infrastructure.otp;

import ministere.sante.senpna.auth.domain.valueobject.OtpChannel;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

@DisplayName("Adaptateurs d'envoi OTP par canal")
class ChannelOtpSenderAdaptersTest {

    @Test
    @DisplayName("LoggingEmailOtpSenderAdapter — canal EMAIL, n'envoie jamais réellement (profil test)")
    void loggingEmailAdapter_canalEmail() {
        LoggingEmailOtpSenderAdapter sut = new LoggingEmailOtpSenderAdapter();

        assertThat(sut.channel()).isEqualTo(OtpChannel.EMAIL);
        assertThatCode(() -> sut.send("test@sante.gouv.sn", "123456")).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("SmsOtpSenderAdapter — canal SMS, journalise sans lever d'exception")
    void smsAdapter_canalSms() {
        SmsOtpSenderAdapter sut = new SmsOtpSenderAdapter();

        assertThat(sut.channel()).isEqualTo(OtpChannel.SMS);
        assertThatCode(() -> sut.send("+221771234567", "123456")).doesNotThrowAnyException();
    }
}
