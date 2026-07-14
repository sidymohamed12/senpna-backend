package ministere.sante.senpna.appeloffre.application.usecase;

import ministere.sante.senpna.appeloffre.application.service.OffreDetailAssembler;
import ministere.sante.senpna.appeloffre.domain.command.OffreFournisseurCommands.OffreDetail;
import ministere.sante.senpna.appeloffre.domain.command.OffreFournisseurCommands.RetenirOffreCommand;
import ministere.sante.senpna.appeloffre.domain.exception.OffreFournisseurIntrouvableException;
import ministere.sante.senpna.appeloffre.domain.model.OffreFournisseur;
import ministere.sante.senpna.appeloffre.domain.port.in.RetenirOffreUseCase;
import ministere.sante.senpna.appeloffre.domain.port.out.OffreFournisseurRepositoryPort;
import ministere.sante.senpna.appeloffre.domain.valueobject.OffreFournisseurId;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Retenir une offre isolément (hors attribution globale de l'AO — cf.
 * {@code AttribuerAppelOffreUseCaseImpl}), utile lorsque la PNA affine sa
 * décision avant l'attribution finale.
 */
@Service
public class RetenirOffreUseCaseImpl implements RetenirOffreUseCase {

    private final OffreFournisseurRepositoryPort offreFournisseurRepositoryPort;
    private final OffreDetailAssembler offreDetailAssembler;

    public RetenirOffreUseCaseImpl(OffreFournisseurRepositoryPort offreFournisseurRepositoryPort,
            OffreDetailAssembler offreDetailAssembler) {
        this.offreFournisseurRepositoryPort = offreFournisseurRepositoryPort;
        this.offreDetailAssembler = offreDetailAssembler;
    }

    @Override
    @Transactional
    public OffreDetail retenir(RetenirOffreCommand command) {
        OffreFournisseur offre = offreFournisseurRepositoryPort.findById(OffreFournisseurId.of(command.offreId()))
                .orElseThrow(OffreFournisseurIntrouvableException::new);

        offre.retenir();

        OffreFournisseur saved = offreFournisseurRepositoryPort.save(offre);
        return offreDetailAssembler.assembler(saved);
    }
}
