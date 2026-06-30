package ministere.sante.senpna.auth.application.service;

import ministere.sante.senpna.auth.domain.model.User;
import ministere.sante.senpna.auth.domain.valueobject.OtpChannel;
import ministere.sante.senpna.shared.infrastructure.exception.BusinessRuleException;

import org.springframework.stereotype.Component;

/**
 * Résout la destination physique (e-mail ou téléphone) d'envoi de l'OTP
 * en fonction du canal choisi par l'utilisateur. Mutualisé entre
 * {@code ForgotPasswordService} et {@code ResendOtpService}.
 */
@Component
public class OtpDestinationResolver {

    public String resoudre(User user, OtpChannel channel) {
        return switch (channel) {
            case EMAIL -> user.getEmail().value();
            case SMS -> {
                if (user.getTelephone() == null) {
                    throw new BusinessRuleException(
                            "Aucun numéro de téléphone enregistré pour ce compte. Choisissez le canal e-mail.",
                            "NO_PHONE_REGISTERED");
                }
                yield user.getTelephone().value();
            }
        };
    }
}
