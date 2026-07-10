package ministere.sante.senpna.organisation.application.usecase.affectation;

import ministere.sante.senpna.organisation.domain.command.OrganisationCommands.AssignUserToEntrepotCommand;
import ministere.sante.senpna.organisation.domain.command.OrganisationCommands.UserAffectationDetail;
import ministere.sante.senpna.organisation.domain.exception.EntrepotInactifException;
import ministere.sante.senpna.organisation.domain.exception.EntrepotIntrouvableException;
import ministere.sante.senpna.organisation.domain.model.Entrepot;
import ministere.sante.senpna.organisation.domain.port.in.affectation.AssignUserToEntrepotUseCase;
import ministere.sante.senpna.organisation.domain.port.out.EntrepotRepositoryPort;
import ministere.sante.senpna.shared.domain.port.out.UserAffectationRepositoryPort;
import ministere.sante.senpna.organisation.domain.valueobject.EntrepotId;
import ministere.sante.senpna.shared.domain.exception.UserNotFoundException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Affecte un utilisateur à un entrepôt (PNA centrale ou PRA).
 * métier : "Affectation d'un utilisateur à : une PNA ; une PRA ; une
 * structure sanitaire". Un utilisateur n'est rattaché qu'à une seule unité
 * organisationnelle à la fois : toute affectation à un entrepôt efface une
 * éventuelle affectation préalable à une structure sanitaire.
 */
@Service
public class AssignUserToEntrepotUseCaseImpl implements AssignUserToEntrepotUseCase {

    private final UserAffectationRepositoryPort userAffectationRepositoryPort;
    private final EntrepotRepositoryPort entrepotRepositoryPort;

    public AssignUserToEntrepotUseCaseImpl(UserAffectationRepositoryPort userAffectationRepositoryPort,
            EntrepotRepositoryPort entrepotRepositoryPort) {
        this.userAffectationRepositoryPort = userAffectationRepositoryPort;
        this.entrepotRepositoryPort = entrepotRepositoryPort;
    }

    @Override
    @Transactional
    public UserAffectationDetail affecter(AssignUserToEntrepotCommand command) {
        if (!userAffectationRepositoryPort.existsUtilisateur(command.userId())) {
            throw new UserNotFoundException();
        }

        Entrepot entrepot = entrepotRepositoryPort.findById(EntrepotId.of(command.entrepotId()))
                .orElseThrow(EntrepotIntrouvableException::new);
        if (!entrepot.isActif()) {
            throw new EntrepotInactifException();
        }

        userAffectationRepositoryPort.affecterEntrepot(command.userId(), command.entrepotId());

        return new UserAffectationDetail(command.userId(), command.entrepotId(), null);
    }
}
