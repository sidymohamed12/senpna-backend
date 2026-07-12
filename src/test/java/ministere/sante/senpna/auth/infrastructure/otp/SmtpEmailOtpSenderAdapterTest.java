package ministere.sante.senpna.auth.infrastructure.otp;

import ministere.sante.senpna.auth.domain.exception.OtpEnvoiEchoueException;
import ministere.sante.senpna.auth.domain.valueobject.OtpChannel;
import ministere.sante.senpna.config.AppProperties;

import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("SmtpEmailOtpSenderAdapter")
class SmtpEmailOtpSenderAdapterTest {

    private JavaMailSender javaMailSender;
    private SmtpEmailOtpSenderAdapter sut;

    @BeforeEach
    void setUp() {
        javaMailSender = mock(JavaMailSender.class);
        AppProperties appProperties = new AppProperties(null, null, null, null,
                new AppProperties.MailProperties("no-reply@senpharmaflow.gouv.sn", "SEN PharmaFlow"), null);
        sut = new SmtpEmailOtpSenderAdapter(javaMailSender, appProperties);
    }

    private MimeMessage nouveauMimeMessage() {
        return new MimeMessage(Session.getDefaultInstance(new Properties()));
    }

    @Test
    @DisplayName("canal annoncé est bien EMAIL")
    void channel_estEmail() {
        assertThat(sut.channel()).isEqualTo(OtpChannel.EMAIL);
    }

    @Test
    @DisplayName("envoie un e-mail avec le code, le sujet et le destinataire corrects")
    void send_succes_envoieMessageCorrect() throws Exception {
        MimeMessage message = nouveauMimeMessage();
        when(javaMailSender.createMimeMessage()).thenReturn(message);

        sut.send("user@example.com", "123456");

        ArgumentCaptor<MimeMessage> captor = ArgumentCaptor.forClass(MimeMessage.class);
        verify(javaMailSender).send(captor.capture());

        MimeMessage envoye = captor.getValue();
        assertThat(envoye.getAllRecipients()[0].toString()).hasToString("user@example.com");
        assertThat(envoye.getSubject()).contains("vérification");
        assertThat(envoye.getFrom()[0].toString()).contains("no-reply@senpharmaflow.gouv.sn");
    }

    @Test
    @DisplayName("échec d'envoi SMTP → OtpEnvoiEchoueException")
    void send_echecSmtp_leveException() {
        when(javaMailSender.createMimeMessage()).thenReturn(nouveauMimeMessage());
        doThrow(new MailSendException("SMTP indisponible")).when(javaMailSender).send(any(MimeMessage.class));

        assertThatThrownBy(() -> sut.send("user@example.com", "123456"))
                .isInstanceOf(OtpEnvoiEchoueException.class);
    }
}
