package ministere.sante.senpna.auth.application.usecase;

import ministere.sante.senpna.auth.application.service.UserAffectationResolver;
import ministere.sante.senpna.auth.application.service.UserRoleResolver;
import ministere.sante.senpna.auth.domain.command.AuthCommands.MeQuery;
import ministere.sante.senpna.auth.domain.command.AuthCommands.UserSummary;
import ministere.sante.senpna.shared.domain.model.User;
import ministere.sante.senpna.shared.domain.projection.UserAffectationView;
import ministere.sante.senpna.auth.domain.port.in.MeUseCase;
import ministere.sante.senpna.auth.domain.port.out.UserRepositoryPort;
import ministere.sante.senpna.shared.domain.valueobject.Email;
import ministere.sante.senpna.shared.domain.exception.UserNotFoundException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
public class MeUseCaseImpl implements MeUseCase {

    private final UserRepositoryPort userRepositoryPort;
    private final UserRoleResolver userRoleResolver;
    private final UserAffectationResolver userAffectationResolver;

    public MeUseCaseImpl(UserRepositoryPort userRepositoryPort, UserRoleResolver userRoleResolver,
            UserAffectationResolver userAffectationResolver) {
        this.userRepositoryPort = userRepositoryPort;
        this.userRoleResolver = userRoleResolver;
        this.userAffectationResolver = userAffectationResolver;
    }

    @Override
    @Transactional(readOnly = true)
    public UserSummary me(MeQuery query) {
        User user = userRepositoryPort.findByEmail(Email.of(query.email()))
                .orElseThrow(UserNotFoundException::new);

        Set<String> roleCodes = userRoleResolver.resoudreCodes(user.getRoleIds());
        UserAffectationView affectation = userAffectationResolver.resoudre(user.getId().getValue());

        return new UserSummary(
                user.getId().getValue(),
                user.getNom().getValue(),
                user.getPrenom().getValue(),
                user.getEmail().value(),
                roleCodes,
                affectation.entrepotId(),
                affectation.structureSanitaireId());
    }
}
