package ministere.sante.senpna.shared.infrastructure.mail;

import ministere.sante.senpna.shared.domain.port.out.AccountMailPort;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Implémentation de secours de {@link AccountMailPort} — journalise au
 * lieu d'envoyer réellement l'e-mail, en attendant le branchement d'un
 * client e-mail définitif (même maturité que
 * {@code EmailOtpSenderAdapter} dans le module {@code auth}, qui n'est
 * pas non plus encore relié à un fournisseur SMTP/SendGrid réel).
 */
@Component
public class LoggingAccountMailAdapter implements AccountMailPort {

    private static final Logger log = LoggerFactory.getLogger(LoggingAccountMailAdapter.class);

    @Override
    public void envoyerIdentifiantsCompte(String email, String nom, String prenom, String motDePasseTemporaire) {
        // TODO: brancher le client e-mail définitif (JavaMailSender / SendGrid / etc.)
        log.info(
                "[MAIL][COMPTE] (provider non configuré — log de secours) destinataire={}, "
                        + "titulaire={} {}, motDePasseTemporaire={}",
                email, prenom, nom, motDePasseTemporaire);
    }
}
