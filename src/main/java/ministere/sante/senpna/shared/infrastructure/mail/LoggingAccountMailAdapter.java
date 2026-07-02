package ministere.sante.senpna.shared.infrastructure.mail;

import ministere.sante.senpna.shared.domain.port.out.AccountMailPort;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Journalise au lieu d'envoyer réellement l'e-mail — actif uniquement en
 * profil {@code test}, afin qu'aucun test ne déclenche d'appel SMTP réel
 * (cf. {@code SmtpAccountMailAdapter}, actif en dev/prod).
 */
@Component
@Profile("test")
public class LoggingAccountMailAdapter implements AccountMailPort {

    private static final Logger log = LoggerFactory.getLogger(LoggingAccountMailAdapter.class);

    @Override
    public void envoyerIdentifiantsCompte(String email, String nom, String prenom, String motDePasseTemporaire) {
        log.info(
                "[MAIL][COMPTE] (profil test — aucun envoi réel) destinataire={}, "
                        + "titulaire={} {}, motDePasseTemporaire={}",
                email, prenom, nom, motDePasseTemporaire);
    }
}
