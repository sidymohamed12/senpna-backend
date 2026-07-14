package ministere.sante.senpna.appeloffre.application.usecase;

import ministere.sante.senpna.appeloffre.application.service.OffreDetailAssembler;
import ministere.sante.senpna.appeloffre.domain.command.OffreFournisseurCommands.OffreDetail;
import ministere.sante.senpna.appeloffre.domain.command.OffreFournisseurCommands.RejeterOffreCommand;
import ministere.sante.senpna.appeloffre.domain.exception.OffreFournisseurIntrouvableException;
import ministere.sante.senpna.appeloffre.domain.model.OffreFournisseur;
import ministere.sante.senpna.appeloffre.domain.port.in.RejeterOffreUseCase;
import ministere.sante.senpna.appeloffre.domain.port.out.OffreFournisseurRepositoryPort;
import ministere.sante.senpna.appeloffre.domain.valueobject.OffreFournisseurId;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RejeterOffreUseCaseImpl implements RejeterOffreUseCase {

    private final OffreFournisseurRepositoryPort offreFournisseurRepositoryPort;
    private final OffreDetailAssembler offreDetailAssembler;

    public RejeterOffreUseCaseImpl(OffreFournisseurRepositoryPort offreFournisseurRepositoryPort,
            OffreDetailAssembler offreDetailAssembler) {
        this.offreFournisseurRepositoryPort = offreFournisseurRepositoryPort;
        this.offreDetailAssembler = offreDetailAssembler;
    }

    @Override
    @Transactional
    public OffreDetail rejeter(RejeterOffreCommand command) {
        OffreFournisseur offre = offreFournisseurRepositoryPort.findById(OffreFournisseurId.of(command.offreId()))
                .orElseThrow(OffreFournisseurIntrouvableException::new);

        offre.rejeter();

        OffreFournisseur saved = offreFournisseurRepositoryPort.save(offre);
        return offreDetailAssembler.assembler(saved);
    }
}
