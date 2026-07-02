package ministere.sante.senpna.fournisseur.application.usecase;

import ministere.sante.senpna.fournisseur.application.service.FournisseurDetailAssembler;
import ministere.sante.senpna.fournisseur.domain.command.FournisseurCommands.CreateFournisseurCommand;
import ministere.sante.senpna.fournisseur.domain.command.FournisseurCommands.FournisseurDetail;
import ministere.sante.senpna.fournisseur.domain.exception.NomFournisseurDejaUtiliseException;
import ministere.sante.senpna.fournisseur.domain.model.Fournisseur;
import ministere.sante.senpna.fournisseur.domain.port.in.CreateFournisseurUseCase;
import ministere.sante.senpna.fournisseur.domain.port.out.FournisseurRepositoryPort;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreateFournisseurUseCaseImpl implements CreateFournisseurUseCase {

    private final FournisseurRepositoryPort fournisseurRepositoryPort;
    private final FournisseurDetailAssembler fournisseurDetailAssembler;

    public CreateFournisseurUseCaseImpl(FournisseurRepositoryPort fournisseurRepositoryPort,
            FournisseurDetailAssembler fournisseurDetailAssembler) {
        this.fournisseurRepositoryPort = fournisseurRepositoryPort;
        this.fournisseurDetailAssembler = fournisseurDetailAssembler;
    }

    @Override
    @Transactional
    public FournisseurDetail creer(CreateFournisseurCommand command) {
        if (fournisseurRepositoryPort.existsByNomIgnoreCase(command.nom().trim())) {
            throw new NomFournisseurDejaUtiliseException(command.nom());
        }

        Fournisseur fournisseur = Fournisseur.creer(command.nom(), command.adresse(), command.telephone(),
                command.email(), command.contactPrincipal());

        Fournisseur saved = fournisseurRepositoryPort.save(fournisseur);
        return fournisseurDetailAssembler.assembler(saved);
    }
}
