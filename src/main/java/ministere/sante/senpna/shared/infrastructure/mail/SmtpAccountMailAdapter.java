package ministere.sante.senpna.shared.infrastructure.mail;

import ministere.sante.senpna.config.AppProperties;
import ministere.sante.senpna.shared.domain.port.out.AccountMailPort;

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
 * Envoi réel des identifiants de compte par e-mail via SMTP. Actif en dev
 * et prod ; remplacé en profil {@code test} par
 * {@link LoggingAccountMailAdapter}.
 *
 * <p>
 * Contrairement à l'envoi d'OTP (chemin critique, cf.
 * {@code SmtpEmailOtpSenderAdapter}), une notification de compte est une
 * information différée à effort <strong>best-effort</strong> : un échec
 * d'envoi est journalisé mais n'est jamais propagé, pour ne jamais faire
 * échouer/annuler la création du compte elle-même à cause d'un problème
 * de transport e-mail (le titulaire pourra toujours réinitialiser son
 * mot de passe via le circuit OTP).
 * </p>
 */
@Component
@Profile({ "dev", "prod" })
public class SmtpAccountMailAdapter implements AccountMailPort {

    private static final Logger log = LoggerFactory.getLogger(SmtpAccountMailAdapter.class);

    private final JavaMailSender javaMailSender;
    private final AppProperties appProperties;

    public SmtpAccountMailAdapter(JavaMailSender javaMailSender, AppProperties appProperties) {
        this.javaMailSender = javaMailSender;
        this.appProperties = appProperties;
    }

    @Override
    public void envoyerIdentifiantsCompte(String email, String nom, String prenom, String motDePasseTemporaire) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(appProperties.mail().from(), appProperties.mail().fromName());
            helper.setTo(email);
            helper.setSubject("Votre compte SEN PharmaFlow a été créé");
            helper.setText(corpsTexte(prenom, nom, email, motDePasseTemporaire),
                    corpsHtml(prenom, nom, email, motDePasseTemporaire));

            javaMailSender.send(message);
        } catch (MessagingException | UnsupportedEncodingException | MailException e) {
            log.error("[MAIL][COMPTE] Échec de l'envoi vers {} : {}", email, e.getMessage(), e);
            // Best-effort : on n'interrompt jamais la création du compte pour un
            // problème de transport e-mail.
        }
    }

    private String corpsTexte(String prenom, String nom, String email, String motDePasseTemporaire) {
        return "Bonjour " + prenom + " " + nom + ",\n\n"
                + "Votre compte SEN PharmaFlow a été créé.\n"
                + "Identifiant : " + email + "\n"
                + "Mot de passe temporaire : " + motDePasseTemporaire + "\n\n"
                + "Merci de changer ce mot de passe dès votre première connexion.";
    }

    private String corpsHtml(String prenom, String nom, String email, String motDePasseTemporaire) {
        return """
                <div style="font-family:Arial,sans-serif;max-width:480px;margin:0 auto">
                  <h2 style="color:#0a5c36">SEN PharmaFlow</h2>
                  <p>Bonjour %s %s,</p>
                  <p>Votre compte a été créé. Voici vos identifiants de connexion :</p>
                  <table style="margin:16px 0">
                    <tr><td style="padding:4px 12px 4px 0;color:#666">Identifiant</td><td><strong>%s</strong></td></tr>
                    <tr><td style="padding:4px 12px 4px 0;color:#666">Mot de passe temporaire</td><td><strong>%s</strong></td></tr>
                  </table>
                  <p style="color:#666;font-size:13px">
                    Merci de changer ce mot de passe dès votre première connexion.
                  </p>
                </div>
                """
                .formatted(prenom, nom, email, motDePasseTemporaire);
    }
}
