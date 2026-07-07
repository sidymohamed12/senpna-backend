package ministere.sante.senpna.carriere.application.usecase;

import ministere.sante.senpna.carriere.application.service.OpportuniteCarriereCommandMapper;
import ministere.sante.senpna.carriere.application.service.OpportuniteCarriereDetailAssembler;
import ministere.sante.senpna.carriere.domain.command.OpportuniteCarriereCommands.CreateOpportuniteCarriereCommand;
import ministere.sante.senpna.carriere.domain.command.OpportuniteCarriereCommands.OpportuniteCarriereDetail;
import ministere.sante.senpna.carriere.domain.model.OpportuniteCarriere;
import ministere.sante.senpna.carriere.domain.port.in.CreateOpportuniteCarriereUseCase;
import ministere.sante.senpna.carriere.domain.port.out.OpportuniteCarriereRepositoryPort;
import ministere.sante.senpna.carriere.domain.valueobject.TypeContrat;
import ministere.sante.senpna.shared.domain.exception.UserNotFoundException;
import ministere.sante.senpna.shared.domain.model.User;
import ministere.sante.senpna.shared.domain.port.out.UserManagementRepositoryPort;
import ministere.sante.senpna.shared.domain.valueobject.UserId;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreateOpportuniteCarriereUseCaseImpl implements CreateOpportuniteCarriereUseCase {

    private final OpportuniteCarriereRepositoryPort opportuniteCarriereRepositoryPort;
    private final UserManagementRepositoryPort userManagementRepositoryPort;
    private final OpportuniteCarriereCommandMapper commandMapper;
    private final OpportuniteCarriereDetailAssembler assembler;

    public CreateOpportuniteCarriereUseCaseImpl(OpportuniteCarriereRepositoryPort opportuniteCarriereRepositoryPort,
            UserManagementRepositoryPort userManagementRepositoryPort, OpportuniteCarriereCommandMapper commandMapper,
            OpportuniteCarriereDetailAssembler assembler) {
        this.opportuniteCarriereRepositoryPort = opportuniteCarriereRepositoryPort;
        this.userManagementRepositoryPort = userManagementRepositoryPort;
        this.commandMapper = commandMapper;
        this.assembler = assembler;
    }

    @Override
    @Transactional
    public OpportuniteCarriereDetail creer(CreateOpportuniteCarriereCommand command) {
        User auteur = userManagementRepositoryPort.findById(UserId.of(command.auteurId()))
                .orElseThrow(UserNotFoundException::new);

        TypeContrat typeContrat = commandMapper.versTypeContrat(command.typeContrat());
        String auteurNom = auteur.getPrenom().getValue() + " " + auteur.getNom().getValue();

        OpportuniteCarriere opportunite = OpportuniteCarriere.creer(
                command.titre(), command.nomEntreprise(), command.description(), command.ficheDePosteUrl(),
                command.lieu(), typeContrat, command.dateDebut(), command.dateLimiteCandidature(),
                command.auteurId(), auteurNom, command.emailContact());

        OpportuniteCarriere saved = opportuniteCarriereRepositoryPort.save(opportunite);

        return assembler.assembler(saved);
    }
}
