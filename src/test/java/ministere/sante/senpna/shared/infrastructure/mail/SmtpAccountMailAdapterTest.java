package ministere.sante.senpna.shared.infrastructure.mail;

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
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("SmtpAccountMailAdapter")
class SmtpAccountMailAdapterTest {

    private JavaMailSender javaMailSender;
    private SmtpAccountMailAdapter sut;

    @BeforeEach
    void setUp() {
        javaMailSender = mock(JavaMailSender.class);
        AppProperties appProperties = new AppProperties(null, null, null, null,
                new AppProperties.MailProperties("no-reply@senpharmaflow.gouv.sn", "SEN PharmaFlow"), null);
        sut = new SmtpAccountMailAdapter(javaMailSender, appProperties);
    }

    private MimeMessage nouveauMimeMessage() {
        return new MimeMessage(Session.getDefaultInstance(new Properties()));
    }

    @Test
    @DisplayName("envoie un e-mail avec les identifiants et le destinataire corrects")
    void envoyerIdentifiantsCompte_succes_envoieMessageCorrect() throws Exception {
        MimeMessage message = nouveauMimeMessage();
        when(javaMailSender.createMimeMessage()).thenReturn(message);

        sut.envoyerIdentifiantsCompte("jean.diop@sante.gouv.sn", "Diop", "Jean", "TempPass1!23");

        ArgumentCaptor<MimeMessage> captor = ArgumentCaptor.forClass(MimeMessage.class);
        verify(javaMailSender).send(captor.capture());

        MimeMessage envoye = captor.getValue();
        assertThat(envoye.getAllRecipients()[0].toString()).hasToString("jean.diop@sante.gouv.sn");
        assertThat(envoye.getSubject()).contains("compte");
    }

    @Test
    @DisplayName("échec d'envoi SMTP → avalé silencieusement (best-effort), ne remonte jamais")
    void envoyerIdentifiantsCompte_echecSmtp_neLevePasException() {
        when(javaMailSender.createMimeMessage()).thenReturn(nouveauMimeMessage());
        doThrow(new MailSendException("SMTP indisponible")).when(javaMailSender).send(any(MimeMessage.class));

        assertThatCode(() -> sut.envoyerIdentifiantsCompte("jean.diop@sante.gouv.sn", "Diop", "Jean", "TempPass1!23"))
                .doesNotThrowAnyException();
    }
}
