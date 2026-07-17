package ministere.sante.senpna.fournisseur.application.usecase;

import ministere.sante.senpna.fournisseur.application.service.FournisseurDetailAssembler;
import ministere.sante.senpna.fournisseur.domain.command.FournisseurCommands.CreateFournisseurCommand;
import ministere.sante.senpna.fournisseur.domain.command.FournisseurCommands.FournisseurDetail;
import ministere.sante.senpna.fournisseur.domain.exception.NomFournisseurDejaUtiliseException;
import ministere.sante.senpna.fournisseur.domain.model.Fournisseur;
import ministere.sante.senpna.fournisseur.domain.port.in.CreateFournisseurUseCase;
import ministere.sante.senpna.fournisseur.domain.port.out.FournisseurRepositoryPort;
import ministere.sante.senpna.shared.domain.port.out.FournisseurCachePort;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreateFournisseurUseCaseImpl implements CreateFournisseurUseCase {

    private final FournisseurRepositoryPort fournisseurRepositoryPort;
    private final FournisseurCachePort fournisseurCachePort;
    private final FournisseurDetailAssembler fournisseurDetailAssembler;

    public CreateFournisseurUseCaseImpl(FournisseurRepositoryPort fournisseurRepositoryPort,
            FournisseurCachePort fournisseurCachePort, FournisseurDetailAssembler fournisseurDetailAssembler) {
        this.fournisseurRepositoryPort = fournisseurRepositoryPort;
        this.fournisseurCachePort = fournisseurCachePort;
        this.fournisseurDetailAssembler = fournisseurDetailAssembler;
    }

    @Override
    @Transactional
    public FournisseurDetail creer(CreateFournisseurCommand command) {
        if (fournisseurRepositoryPort.existsByNomIgnoreCase(command.nom().trim())) {
            throw new NomFournisseurDejaUtiliseException(command.nom());
        }

        Fournisseur fournisseur = Fournisseur.creer(new Fournisseur.CreationCommand(command.nom(), command.adresse(), command.telephone(),
                command.email(), command.contactPrincipal()));

        Fournisseur saved = fournisseurRepositoryPort.save(fournisseur);

        // Recharge le cache immédiatement : le nouveau fournisseur doit
        // être utilisable sans délai par les autres features (ex :
        // création d'un lot dans la foulée), sans attendre un rechargement
        // périodique — même principe que RegionCachePort.reload().
        fournisseurCachePort.reload();

        return fournisseurDetailAssembler.assembler(saved);
    }
}
