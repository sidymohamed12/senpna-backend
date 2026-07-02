package ministere.sante.senpna.organisation.application.usecase;

import ministere.sante.senpna.organisation.domain.command.OrganisationCommands.UnassignUserCommand;
import ministere.sante.senpna.organisation.domain.command.OrganisationCommands.UserAffectationDetail;
import ministere.sante.senpna.organisation.domain.port.in.UnassignUserUseCase;
import ministere.sante.senpna.organisation.domain.port.out.UserAffectationRepositoryPort;
import ministere.sante.senpna.shared.domain.exception.UserNotFoundException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UnassignUserUseCaseImpl implements UnassignUserUseCase {

    private final UserAffectationRepositoryPort userAffectationRepositoryPort;

    public UnassignUserUseCaseImpl(UserAffectationRepositoryPort userAffectationRepositoryPort) {
        this.userAffectationRepositoryPort = userAffectationRepositoryPort;
    }

    @Override
    @Transactional
    public UserAffectationDetail retirer(UnassignUserCommand command) {
        if (!userAffectationRepositoryPort.existsUtilisateur(command.userId())) {
            throw new UserNotFoundException();
        }

        userAffectationRepositoryPort.retirerAffectation(command.userId());

        return new UserAffectationDetail(command.userId(), null, null);
    }
}
