package ministere.sante.senpna.auth.infrastructure.otp;

import ministere.sante.senpna.auth.domain.valueobject.OtpChannel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Journalise au lieu d'envoyer réellement l'OTP par e-mail — actif
 * uniquement en profil {@code test}, afin qu'aucun test ne déclenche
 * d'appel SMTP réel (cf. {@link SmtpEmailOtpSenderAdapter}, actif en
 * dev/prod).
 */
@Component
@Profile("test")
public class LoggingEmailOtpSenderAdapter implements ChannelOtpSender {

    private static final Logger log = LoggerFactory.getLogger(LoggingEmailOtpSenderAdapter.class);

    @Override
    public OtpChannel channel() {
        return OtpChannel.EMAIL;
    }

    @Override
    public void send(String destination, String code) {
        log.info("[OTP][EMAIL] (profil test — aucun envoi réel) destination={}, code={}", destination, code);
    }
}
