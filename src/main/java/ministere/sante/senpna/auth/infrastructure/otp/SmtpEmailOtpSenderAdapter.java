package ministere.sante.senpna.auth.infrastructure.otp;

import ministere.sante.senpna.auth.domain.exception.OtpEnvoiEchoueException;
import ministere.sante.senpna.auth.domain.valueobject.OtpChannel;
import ministere.sante.senpna.config.AppProperties;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;

/**
 * Envoi réel du code OTP par e-mail via SMTP (transport configuré par
 * {@code spring.mail.*}, auto-configuré par Spring Boot). Actif en dev et
 * prod ; remplacé en profil {@code test} par
 * {@link LoggingEmailOtpSenderAdapter} (aucun accès réseau pendant les
 * tests).
 *
 * <p>
 * Échec d'envoi = chemin critique : l'utilisateur attend activement son
 * code pour continuer sa connexion, donc l'échec est propagé
 * ({@link OtpEnvoiEchoueException}) plutôt qu'avalé silencieusement —
 * contrairement à {@code AccountMailPort}, notification différée à effort
 * best-effort.
 * </p>
 */
@Component
@Profile({ "dev", "prod" })
public class SmtpEmailOtpSenderAdapter implements ChannelOtpSender {

    private static final Logger log = LoggerFactory.getLogger(SmtpEmailOtpSenderAdapter.class);

    private final JavaMailSender javaMailSender;
    private final AppProperties appProperties;

    public SmtpEmailOtpSenderAdapter(JavaMailSender javaMailSender, AppProperties appProperties) {
        this.javaMailSender = javaMailSender;
        this.appProperties = appProperties;
    }

    @Override
    public OtpChannel channel() {
        return OtpChannel.EMAIL;
    }

    @Override
    public void send(String destination, String code) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(appProperties.mail().from(), appProperties.mail().fromName());
            helper.setTo(destination);
            helper.setSubject("Votre code de vérification SEN PharmaFlow");
            helper.setText(corpsTexte(code), corpsHtml(code));

            javaMailSender.send(message);
        } catch (MessagingException | UnsupportedEncodingException | MailException e) {
            log.error("[OTP][EMAIL] Échec de l'envoi vers {} : {}", destination, e.getMessage(), e);
            throw new OtpEnvoiEchoueException();
        }
    }

    private String corpsTexte(String code) {
        return "Votre code de vérification SEN PharmaFlow est : " + code
                + "\n\nCe code expire dans quelques minutes. Ne le partagez avec personne.";
    }

    private String corpsHtml(String code) {
        return """
                <div style="font-family:Arial,sans-serif;max-width:480px;margin:0 auto">
                  <h2 style="color:#0a5c36">SEN PharmaFlow</h2>
                  <p>Votre code de vérification est :</p>
                  <p style="font-size:28px;font-weight:bold;letter-spacing:6px;color:#0a5c36">%s</p>
                  <p style="color:#666;font-size:13px">
                    Ce code expire dans quelques minutes. Ne le partagez avec personne.
                    Si vous n'êtes pas à l'origine de cette demande, ignorez cet e-mail.
                  </p>
                </div>
                """.formatted(code);
    }
}
