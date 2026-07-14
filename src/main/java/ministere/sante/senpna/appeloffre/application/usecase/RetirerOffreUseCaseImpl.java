package ministere.sante.senpna.appeloffre.application.usecase;

import ministere.sante.senpna.appeloffre.application.service.OffreDetailAssembler;
import ministere.sante.senpna.appeloffre.domain.command.OffreFournisseurCommands.OffreDetail;
import ministere.sante.senpna.appeloffre.domain.command.OffreFournisseurCommands.RetirerOffreCommand;
import ministere.sante.senpna.appeloffre.domain.exception.AccesOffreRefuseException;
import ministere.sante.senpna.appeloffre.domain.exception.OffreFournisseurIntrouvableException;
import ministere.sante.senpna.appeloffre.domain.model.OffreFournisseur;
import ministere.sante.senpna.appeloffre.domain.port.in.RetirerOffreUseCase;
import ministere.sante.senpna.appeloffre.domain.port.out.OffreFournisseurRepositoryPort;
import ministere.sante.senpna.appeloffre.domain.valueobject.OffreFournisseurId;
import ministere.sante.senpna.fournisseur.domain.valueobject.FournisseurId;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RetirerOffreUseCaseImpl implements RetirerOffreUseCase {

    private final OffreFournisseurRepositoryPort offreFournisseurRepositoryPort;
    private final OffreDetailAssembler offreDetailAssembler;

    public RetirerOffreUseCaseImpl(OffreFournisseurRepositoryPort offreFournisseurRepositoryPort,
            OffreDetailAssembler offreDetailAssembler) {
        this.offreFournisseurRepositoryPort = offreFournisseurRepositoryPort;
        this.offreDetailAssembler = offreDetailAssembler;
    }

    @Override
    @Transactional
    public OffreDetail retirer(RetirerOffreCommand command) {
        OffreFournisseur offre = offreFournisseurRepositoryPort.findById(OffreFournisseurId.of(command.offreId()))
                .orElseThrow(OffreFournisseurIntrouvableException::new);

        if (!offre.appartientA(FournisseurId.of(command.fournisseurId()))) {
            throw new AccesOffreRefuseException();
        }

        offre.retirer();

        OffreFournisseur saved = offreFournisseurRepositoryPort.save(offre);
        return offreDetailAssembler.assembler(saved);
    }
}
