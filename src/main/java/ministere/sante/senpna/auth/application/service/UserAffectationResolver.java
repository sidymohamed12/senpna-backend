package ministere.sante.senpna.auth.application.service;

import ministere.sante.senpna.shared.domain.port.out.UserAffectationRepositoryPort;
import ministere.sante.senpna.shared.domain.projection.UserAffectationView;

import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Résout l'affectation organisationnelle (entrepôt / structure sanitaire)
 * d'un utilisateur, via {@link UserAffectationRepositoryPort} — mutualisé
 * entre {@code AuthTokenFactory} (claims du JWT), {@code LoginUseCaseImpl}
 * et {@code MeUseCaseImpl} (exposition dans {@code UserSummary}).
 */
@Component
public class UserAffectationResolver {

    private final UserAffectationRepositoryPort userAffectationRepositoryPort;

    public UserAffectationResolver(UserAffectationRepositoryPort userAffectationRepositoryPort) {
        this.userAffectationRepositoryPort = userAffectationRepositoryPort;
    }

    /**
     * @return la vue d'affectation de l'utilisateur, ou une vue "vide"
     *         (entrepotId et structureSanitaireId à {@code null}) si
     *         l'utilisateur n'est affecté à aucune unité organisationnelle.
     */
    public UserAffectationView resoudre(UUID userId) {
        return userAffectationRepositoryPort.findAffectation(userId)
                .orElse(new UserAffectationView(userId, null, null, null));
    }
}