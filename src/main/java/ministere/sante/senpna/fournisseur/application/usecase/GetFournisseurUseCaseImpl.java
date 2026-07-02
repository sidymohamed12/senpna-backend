package ministere.sante.senpna.fournisseur.application.usecase;

import ministere.sante.senpna.fournisseur.application.service.FournisseurDetailAssembler;
import ministere.sante.senpna.fournisseur.domain.command.FournisseurCommands.FournisseurDetail;
import ministere.sante.senpna.fournisseur.domain.command.FournisseurCommands.GetFournisseurQuery;
import ministere.sante.senpna.fournisseur.domain.exception.FournisseurIntrouvableException;
import ministere.sante.senpna.fournisseur.domain.model.Fournisseur;
import ministere.sante.senpna.fournisseur.domain.port.in.GetFournisseurUseCase;
import ministere.sante.senpna.fournisseur.domain.port.out.FournisseurRepositoryPort;
import ministere.sante.senpna.fournisseur.domain.valueobject.FournisseurId;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GetFournisseurUseCaseImpl implements GetFournisseurUseCase {

    private final FournisseurRepositoryPort fournisseurRepositoryPort;
    private final FournisseurDetailAssembler fournisseurDetailAssembler;

    public GetFournisseurUseCaseImpl(FournisseurRepositoryPort fournisseurRepositoryPort,
            FournisseurDetailAssembler fournisseurDetailAssembler) {
        this.fournisseurRepositoryPort = fournisseurRepositoryPort;
        this.fournisseurDetailAssembler = fournisseurDetailAssembler;
    }

    @Override
    @Transactional(readOnly = true)
    public FournisseurDetail obtenir(GetFournisseurQuery query) {
        Fournisseur fournisseur = fournisseurRepositoryPort.findById(FournisseurId.of(query.fournisseurId()))
                .orElseThrow(FournisseurIntrouvableException::new);
        return fournisseurDetailAssembler.assembler(fournisseur);
    }
}
