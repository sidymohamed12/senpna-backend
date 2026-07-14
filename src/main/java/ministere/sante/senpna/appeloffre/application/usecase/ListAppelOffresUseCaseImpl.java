package ministere.sante.senpna.appeloffre.application.usecase;

import ministere.sante.senpna.appeloffre.application.service.AppelOffreDetailAssembler;
import ministere.sante.senpna.appeloffre.domain.command.AppelOffreCommands.AppelOffrePage;
import ministere.sante.senpna.appeloffre.domain.command.AppelOffreCommands.ListAppelOffresQuery;
import ministere.sante.senpna.appeloffre.domain.criteria.AppelOffreSearchCriteria;
import ministere.sante.senpna.appeloffre.domain.model.AppelOffre;
import ministere.sante.senpna.appeloffre.domain.port.in.ListAppelOffresUseCase;
import ministere.sante.senpna.appeloffre.domain.port.out.AppelOffreRepositoryPort;
import ministere.sante.senpna.shared.domain.valueobject.PageRequest;
import ministere.sante.senpna.shared.domain.valueobject.PageResult;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Vue PNA — tous les statuts confondus (cf.
 * {@link ListAppelOffresPubliesUseCaseImpl} pour l'espace fournisseur, qui
 * ne renvoie que les AO publiés).
 */
@Service
public class ListAppelOffresUseCaseImpl implements ListAppelOffresUseCase {

    private final AppelOffreRepositoryPort appelOffreRepositoryPort;
    private final AppelOffreDetailAssembler appelOffreDetailAssembler;

    public ListAppelOffresUseCaseImpl(AppelOffreRepositoryPort appelOffreRepositoryPort,
            AppelOffreDetailAssembler appelOffreDetailAssembler) {
        this.appelOffreRepositoryPort = appelOffreRepositoryPort;
        this.appelOffreDetailAssembler = appelOffreDetailAssembler;
    }

    @Override
    @Transactional(readOnly = true)
    public AppelOffrePage lister(ListAppelOffresQuery query) {
        AppelOffreSearchCriteria criteria = new AppelOffreSearchCriteria(query.recherche(), query.statut());
        PageRequest pageRequest = PageRequest.of(query.page(), query.size(), query.sortBy(), query.sortDirection());

        PageResult<AppelOffre> result = appelOffreRepositoryPort.search(criteria, pageRequest);

        return new AppelOffrePage(
                result.content().stream().map(appelOffreDetailAssembler::assemblerResume).toList(),
                result.page(),
                result.size(),
                result.totalElements(),
                result.totalPages());
    }
}
