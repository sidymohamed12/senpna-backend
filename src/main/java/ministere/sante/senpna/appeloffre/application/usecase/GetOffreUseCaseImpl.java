package ministere.sante.senpna.appeloffre.application.usecase;

import ministere.sante.senpna.appeloffre.application.service.OffreDetailAssembler;
import ministere.sante.senpna.appeloffre.domain.command.OffreFournisseurCommands.GetOffreQuery;
import ministere.sante.senpna.appeloffre.domain.command.OffreFournisseurCommands.OffreDetail;
import ministere.sante.senpna.appeloffre.domain.exception.AccesOffreRefuseException;
import ministere.sante.senpna.appeloffre.domain.exception.OffreFournisseurIntrouvableException;
import ministere.sante.senpna.appeloffre.domain.model.OffreFournisseur;
import ministere.sante.senpna.appeloffre.domain.port.in.GetOffreUseCase;
import ministere.sante.senpna.appeloffre.domain.port.out.OffreFournisseurRepositoryPort;
import ministere.sante.senpna.appeloffre.domain.valueobject.OffreFournisseurId;
import ministere.sante.senpna.fournisseur.domain.valueobject.FournisseurId;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Consultation d'une offre. Le {@code fournisseurId} de la requête est
 * {@code null} lorsqu'appelée par la PNA (aucune restriction de
 * propriété) et renseigné lorsqu'appelée depuis l'espace fournisseur, où
 * l'accès est alors restreint au propriétaire de l'offre.
 */
@Service
public class GetOffreUseCaseImpl implements GetOffreUseCase {

    private final OffreFournisseurRepositoryPort offreFournisseurRepositoryPort;
    private final OffreDetailAssembler offreDetailAssembler;

    public GetOffreUseCaseImpl(OffreFournisseurRepositoryPort offreFournisseurRepositoryPort,
            OffreDetailAssembler offreDetailAssembler) {
        this.offreFournisseurRepositoryPort = offreFournisseurRepositoryPort;
        this.offreDetailAssembler = offreDetailAssembler;
    }

    @Override
    @Transactional(readOnly = true)
    public OffreDetail obtenir(GetOffreQuery query) {
        OffreFournisseur offre = offreFournisseurRepositoryPort.findById(OffreFournisseurId.of(query.offreId()))
                .orElseThrow(OffreFournisseurIntrouvableException::new);

        if (query.fournisseurId() != null && !offre.appartientA(FournisseurId.of(query.fournisseurId()))) {
            throw new AccesOffreRefuseException();
        }

        return offreDetailAssembler.assembler(offre);
    }
}
