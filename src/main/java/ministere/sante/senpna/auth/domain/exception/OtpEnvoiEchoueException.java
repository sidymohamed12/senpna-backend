package ministere.sante.senpna.auth.domain.exception;

import ministere.sante.senpna.shared.domain.exception.ErrorCategory;
import ministere.sante.senpna.shared.domain.exception.SenPnaException;

/**
 * Levée lorsque l'envoi effectif du code OTP échoue au niveau transport
 * (ex: serveur SMTP injoignable, identifiants invalides). Le code a déjà
 * été généré et placé en cache à ce stade — l'appelant doit redemander un
 * envoi plutôt que réutiliser ce code, qui reste cependant valide jusqu'à
 * expiration si le problème se résout entre-temps.
 */
public class OtpEnvoiEchoueException extends SenPnaException {
    public OtpEnvoiEchoueException() {
        super("L'envoi du code de vérification a échoué. Veuillez réessayer.", "OTP_SEND_FAILED", ErrorCategory.BUSINESS_RULE);
    }
}
