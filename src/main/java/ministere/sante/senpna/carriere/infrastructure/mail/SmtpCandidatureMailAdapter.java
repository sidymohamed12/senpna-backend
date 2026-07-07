package ministere.sante.senpna.carriere.infrastructure.mail;

import ministere.sante.senpna.carriere.domain.port.out.CandidatureMailPort;
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
 * Envoi réel, via SMTP, des e-mails liés à une candidature. Actif en dev et
 * prod ; remplacé en profil {@code test} par
 * {@link LoggingCandidatureMailAdapter}.
 *
 * <p>
 * Best-effort, à l'image de {@code SmtpAccountMailAdapter} : un échec
 * d'envoi est journalisé mais n'est jamais propagé — la candidature a déjà
 * été enregistrée avec succès avant que ce composant ne soit sollicité
 * (consommé après commit par {@code NouvelleCandidatureListener}), un
 * problème de transport e-mail ne doit donc jamais faire apparaître la
 * soumission comme échouée aux yeux du candidat.
 * </p>
 */
@Component
@Profile({ "dev", "prod" })
public class SmtpCandidatureMailAdapter implements CandidatureMailPort {

    private static final Logger log = LoggerFactory.getLogger(SmtpCandidatureMailAdapter.class);

    private final JavaMailSender javaMailSender;
    private final AppProperties appProperties;

    public SmtpCandidatureMailAdapter(JavaMailSender javaMailSender, AppProperties appProperties) {
        this.javaMailSender = javaMailSender;
        this.appProperties = appProperties;
    }

    @Override
    public void envoyerAccuseReceptionCandidature(String email, String nomCandidat, String titreOffre,
            String nomEntreprise) {
        envoyer(email, "Votre candidature a bien été reçue — " + titreOffre,
                corpsTexteAccuse(nomCandidat, titreOffre, nomEntreprise),
                corpsHtmlAccuse(nomCandidat, titreOffre, nomEntreprise));
    }

    @Override
    public void envoyerNotificationNouvelleCandidature(String emailDestinataireRH, String titreOffre,
            String nomEntreprise, String nomCandidat, String emailCandidat, String telephoneCandidat) {
        envoyer(emailDestinataireRH, "Nouvelle candidature reçue — " + titreOffre,
                corpsTexteNotification(titreOffre, nomEntreprise, nomCandidat, emailCandidat, telephoneCandidat),
                corpsHtmlNotification(titreOffre, nomEntreprise, nomCandidat, emailCandidat, telephoneCandidat));
    }

    private void envoyer(String destinataire, String sujet, String corpsTexte, String corpsHtml) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(appProperties.mail().from(), appProperties.mail().fromName());
            helper.setTo(destinataire);
            helper.setSubject(sujet);
            helper.setText(corpsTexte, corpsHtml);

            javaMailSender.send(message);
        } catch (MessagingException | UnsupportedEncodingException | MailException e) {
            log.error("[MAIL][CANDIDATURE] Échec de l'envoi vers {} : {}", destinataire, e.getMessage(), e);
            // Best-effort : cf. Javadoc de classe.
        }
    }

    private String corpsTexteAccuse(String nomCandidat, String titreOffre, String nomEntreprise) {
        return "Bonjour " + nomCandidat + ",\n\n"
                + "Nous avons bien reçu votre candidature pour le poste \"" + titreOffre + "\" chez "
                + nomEntreprise + ".\n"
                + "Notre équipe l'examinera dans les meilleurs délais.\n\n"
                + "Merci de votre intérêt.";
    }

    private String corpsHtmlAccuse(String nomCandidat, String titreOffre, String nomEntreprise) {
        return """
                <div style="font-family:Arial,sans-serif;max-width:480px;margin:0 auto">
                  <h2 style="color:#0a5c36">SEN PharmaFlow</h2>
                  <p>Bonjour %s,</p>
                  <p>Nous avons bien reçu votre candidature pour le poste <strong>%s</strong> chez
                  <strong>%s</strong>.</p>
                  <p>Notre équipe l'examinera dans les meilleurs délais.</p>
                  <p style="color:#666;font-size:13px">Merci de votre intérêt.</p>
                </div>
                """
                .formatted(nomCandidat, titreOffre, nomEntreprise);
    }

    private String corpsTexteNotification(String titreOffre, String nomEntreprise, String nomCandidat,
            String emailCandidat, String telephoneCandidat) {
        return "Une nouvelle candidature a été reçue pour l'offre \"" + titreOffre + "\" (" + nomEntreprise + ").\n\n"
                + "Candidat : " + nomCandidat + "\n"
                + "E-mail : " + emailCandidat + "\n"
                + "Téléphone : " + telephoneCandidat + "\n";
    }

    private String corpsHtmlNotification(String titreOffre, String nomEntreprise, String nomCandidat,
            String emailCandidat, String telephoneCandidat) {
        return """
                <div style="font-family:Arial,sans-serif;max-width:480px;margin:0 auto">
                  <h2 style="color:#0a5c36">SEN PharmaFlow</h2>
                  <p>Une nouvelle candidature a été reçue pour l'offre <strong>%s</strong> (%s).</p>
                  <table style="margin:16px 0">
                    <tr><td style="padding:4px 12px 4px 0;color:#666">Candidat</td><td><strong>%s</strong></td></tr>
                    <tr><td style="padding:4px 12px 4px 0;color:#666">E-mail</td><td><strong>%s</strong></td></tr>
                    <tr><td style="padding:4px 12px 4px 0;color:#666">Téléphone</td><td><strong>%s</strong></td></tr>
                  </table>
                </div>
                """
                .formatted(titreOffre, nomEntreprise, nomCandidat, emailCandidat, telephoneCandidat);
    }
}
