package ministere.sante.senpna.fournisseur.application.usecase;

import ministere.sante.senpna.fournisseur.application.service.FournisseurDetailAssembler;
import ministere.sante.senpna.fournisseur.domain.command.FournisseurCommands.ActivateFournisseurCommand;
import ministere.sante.senpna.fournisseur.domain.command.FournisseurCommands.FournisseurDetail;
import ministere.sante.senpna.fournisseur.domain.exception.FournisseurIntrouvableException;
import ministere.sante.senpna.fournisseur.domain.model.Fournisseur;
import ministere.sante.senpna.fournisseur.domain.port.in.ActivateFournisseurUseCase;
import ministere.sante.senpna.fournisseur.domain.port.out.FournisseurRepositoryPort;
import ministere.sante.senpna.fournisseur.domain.valueobject.FournisseurId;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ActivateFournisseurUseCaseImpl implements ActivateFournisseurUseCase {

    private final FournisseurRepositoryPort fournisseurRepositoryPort;
    private final FournisseurDetailAssembler fournisseurDetailAssembler;

    public ActivateFournisseurUseCaseImpl(FournisseurRepositoryPort fournisseurRepositoryPort,
            FournisseurDetailAssembler fournisseurDetailAssembler) {
        this.fournisseurRepositoryPort = fournisseurRepositoryPort;
        this.fournisseurDetailAssembler = fournisseurDetailAssembler;
    }

    @Override
    @Transactional
    public FournisseurDetail activer(ActivateFournisseurCommand command) {
        Fournisseur fournisseur = fournisseurRepositoryPort.findById(FournisseurId.of(command.fournisseurId()))
                .orElseThrow(FournisseurIntrouvableException::new);

        fournisseur.activer();

        Fournisseur saved = fournisseurRepositoryPort.save(fournisseur);
        return fournisseurDetailAssembler.assembler(saved);
    }
}
