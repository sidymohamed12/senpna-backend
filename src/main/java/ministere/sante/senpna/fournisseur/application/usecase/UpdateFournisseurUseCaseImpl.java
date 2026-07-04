package ministere.sante.senpna.fournisseur.application.usecase;

import ministere.sante.senpna.fournisseur.application.service.FournisseurDetailAssembler;
import ministere.sante.senpna.fournisseur.domain.command.FournisseurCommands.FournisseurDetail;
import ministere.sante.senpna.fournisseur.domain.command.FournisseurCommands.UpdateFournisseurCommand;
import ministere.sante.senpna.fournisseur.domain.exception.FournisseurIntrouvableException;
import ministere.sante.senpna.fournisseur.domain.exception.NomFournisseurDejaUtiliseException;
import ministere.sante.senpna.fournisseur.domain.model.Fournisseur;
import ministere.sante.senpna.fournisseur.domain.port.in.UpdateFournisseurUseCase;
import ministere.sante.senpna.fournisseur.domain.port.out.FournisseurRepositoryPort;
import ministere.sante.senpna.fournisseur.domain.valueobject.FournisseurId;
import ministere.sante.senpna.shared.domain.port.out.FournisseurCachePort;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UpdateFournisseurUseCaseImpl implements UpdateFournisseurUseCase {

    private final FournisseurRepositoryPort fournisseurRepositoryPort;
    private final FournisseurCachePort fournisseurCachePort;
    private final FournisseurDetailAssembler fournisseurDetailAssembler;

    public UpdateFournisseurUseCaseImpl(FournisseurRepositoryPort fournisseurRepositoryPort,
            FournisseurCachePort fournisseurCachePort, FournisseurDetailAssembler fournisseurDetailAssembler) {
        this.fournisseurRepositoryPort = fournisseurRepositoryPort;
        this.fournisseurCachePort = fournisseurCachePort;
        this.fournisseurDetailAssembler = fournisseurDetailAssembler;
    }

    @Override
    @Transactional
    public FournisseurDetail modifier(UpdateFournisseurCommand command) {
        Fournisseur fournisseur = fournisseurRepositoryPort.findById(FournisseurId.of(command.fournisseurId()))
                .orElseThrow(FournisseurIntrouvableException::new);

        String nouveauNom = command.nom().trim();
        boolean nomInchange = fournisseur.getNom().equalsIgnoreCase(nouveauNom);
        if (!nomInchange
                && fournisseurRepositoryPort.existsByNomIgnoreCaseAndIdNot(nouveauNom, fournisseur.getId())) {
            throw new NomFournisseurDejaUtiliseException(command.nom());
        }

        fournisseur.modifierInformations(command.nom(), command.adresse(), command.telephone(), command.email(),
                command.contactPrincipal());

        Fournisseur saved = fournisseurRepositoryPort.save(fournisseur);
        fournisseurCachePort.reload();

        return fournisseurDetailAssembler.assembler(saved);
    }
}
