package ministere.sante.senpna.carriere.infrastructure.mail;

import ministere.sante.senpna.carriere.domain.port.out.CandidatureMailPort;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Journalise au lieu d'envoyer réellement l'e-mail — actif uniquement en
 * profil {@code test}, afin qu'aucun test ne déclenche d'appel SMTP réel
 * (cf. {@code SmtpCandidatureMailAdapter}, actif en dev/prod).
 */
@Component
@Profile("test")
public class LoggingCandidatureMailAdapter implements CandidatureMailPort {

    private static final Logger log = LoggerFactory.getLogger(LoggingCandidatureMailAdapter.class);

    @Override
    public void envoyerAccuseReceptionCandidature(String email, String nomCandidat, String titreOffre,
            String nomEntreprise) {
        log.info("[MAIL][CANDIDATURE] (profil test — aucun envoi réel) accusé de réception → destinataire={}, "
                + "candidat={}, offre={}, entreprise={}", email, nomCandidat, titreOffre, nomEntreprise);
    }

    @Override
    public void envoyerNotificationNouvelleCandidature(String emailDestinataireRH, String titreOffre,
            String nomEntreprise, String nomCandidat, String emailCandidat, String telephoneCandidat) {
        log.info("[MAIL][CANDIDATURE] (profil test — aucun envoi réel) notification RH → destinataire={}, "
                + "offre={}, entreprise={}, candidat={}, emailCandidat={}, telephoneCandidat={}",
                emailDestinataireRH, titreOffre, nomEntreprise, nomCandidat, emailCandidat, telephoneCandidat);
    }
}
