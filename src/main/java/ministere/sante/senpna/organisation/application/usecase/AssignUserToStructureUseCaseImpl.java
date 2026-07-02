package ministere.sante.senpna.organisation.application.usecase;

import ministere.sante.senpna.organisation.domain.command.OrganisationCommands.AssignUserToStructureCommand;
import ministere.sante.senpna.organisation.domain.command.OrganisationCommands.UserAffectationDetail;
import ministere.sante.senpna.organisation.domain.exception.StructureSanitaireIntrouvableException;
import ministere.sante.senpna.organisation.domain.exception.StructureSanitaireNonValideeException;
import ministere.sante.senpna.organisation.domain.model.StructureSanitaire;
import ministere.sante.senpna.organisation.domain.port.in.AssignUserToStructureUseCase;
import ministere.sante.senpna.organisation.domain.port.out.StructureSanitaireRepositoryPort;
import ministere.sante.senpna.organisation.domain.port.out.UserAffectationRepositoryPort;
import ministere.sante.senpna.organisation.domain.valueobject.StatutAdhesion;
import ministere.sante.senpna.organisation.domain.valueobject.StructureSanitaireId;
import ministere.sante.senpna.shared.domain.exception.UserNotFoundException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AssignUserToStructureUseCaseImpl implements AssignUserToStructureUseCase {

    private final UserAffectationRepositoryPort userAffectationRepositoryPort;
    private final StructureSanitaireRepositoryPort structureSanitaireRepositoryPort;

    public AssignUserToStructureUseCaseImpl(UserAffectationRepositoryPort userAffectationRepositoryPort,
            StructureSanitaireRepositoryPort structureSanitaireRepositoryPort) {
        this.userAffectationRepositoryPort = userAffectationRepositoryPort;
        this.structureSanitaireRepositoryPort = structureSanitaireRepositoryPort;
    }

    @Override
    @Transactional
    public UserAffectationDetail affecter(AssignUserToStructureCommand command) {
        if (!userAffectationRepositoryPort.existsUtilisateur(command.userId())) {
            throw new UserNotFoundException();
        }

        StructureSanitaire structure = structureSanitaireRepositoryPort
                .findById(StructureSanitaireId.of(command.structureId()))
                .orElseThrow(StructureSanitaireIntrouvableException::new);
        if (structure.getStatutAdhesion() != StatutAdhesion.VALIDEE) {
            throw new StructureSanitaireNonValideeException();
        }

        userAffectationRepositoryPort.affecterStructureSanitaire(command.userId(), command.structureId());

        return new UserAffectationDetail(command.userId(), null, command.structureId());
    }
}
