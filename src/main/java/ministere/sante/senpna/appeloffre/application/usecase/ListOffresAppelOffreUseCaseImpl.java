package ministere.sante.senpna.appeloffre.application.usecase;

import ministere.sante.senpna.appeloffre.application.service.OffreDetailAssembler;
import ministere.sante.senpna.appeloffre.domain.command.OffreFournisseurCommands.ListOffresAppelOffreQuery;
import ministere.sante.senpna.appeloffre.domain.command.OffreFournisseurCommands.OffrePage;
import ministere.sante.senpna.appeloffre.domain.model.OffreFournisseur;
import ministere.sante.senpna.appeloffre.domain.port.in.ListOffresAppelOffreUseCase;
import ministere.sante.senpna.appeloffre.domain.port.out.OffreFournisseurRepositoryPort;
import ministere.sante.senpna.appeloffre.domain.valueobject.AppelOffreId;
import ministere.sante.senpna.shared.domain.valueobject.PageRequest;
import ministere.sante.senpna.shared.domain.valueobject.PageResult;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Réservé PNA — analyse comparative des offres reçues pour un AO donné. */
@Service
public class ListOffresAppelOffreUseCaseImpl implements ListOffresAppelOffreUseCase {

    private final OffreFournisseurRepositoryPort offreFournisseurRepositoryPort;
    private final OffreDetailAssembler offreDetailAssembler;

    public ListOffresAppelOffreUseCaseImpl(OffreFournisseurRepositoryPort offreFournisseurRepositoryPort,
            OffreDetailAssembler offreDetailAssembler) {
        this.offreFournisseurRepositoryPort = offreFournisseurRepositoryPort;
        this.offreDetailAssembler = offreDetailAssembler;
    }

    @Override
    @Transactional(readOnly = true)
    public OffrePage lister(ListOffresAppelOffreQuery query) {
        PageRequest pageRequest = PageRequest.of(query.page(), query.size(), null, null);

        PageResult<OffreFournisseur> result = offreFournisseurRepositoryPort.findByAppelOffreId(
                AppelOffreId.of(query.appelOffreId()), pageRequest);

        return new OffrePage(
                result.content().stream().map(offreDetailAssembler::assembler).toList(),
                result.page(),
                result.size(),
                result.totalElements(),
                result.totalPages());
    }
}
