package ministere.sante.senpna.appeloffre.application.usecase;

import ministere.sante.senpna.appeloffre.application.service.AppelOffreDetailAssembler;
import ministere.sante.senpna.appeloffre.domain.command.AppelOffreCommands.AppelOffrePage;
import ministere.sante.senpna.appeloffre.domain.command.AppelOffreCommands.ListAppelOffresQuery;
import ministere.sante.senpna.appeloffre.domain.criteria.AppelOffreSearchCriteria;
import ministere.sante.senpna.appeloffre.domain.model.AppelOffre;
import ministere.sante.senpna.appeloffre.domain.port.in.ListAppelOffresPubliesUseCase;
import ministere.sante.senpna.appeloffre.domain.port.out.AppelOffreRepositoryPort;
import ministere.sante.senpna.appeloffre.domain.valueobject.StatutAppelOffre;
import ministere.sante.senpna.shared.domain.valueobject.PageRequest;
import ministere.sante.senpna.shared.domain.valueobject.PageResult;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Espace fournisseur — le statut est délibérément forcé à {@code PUBLIE},
 * quoi que le client envoie : un fournisseur ne doit jamais pouvoir
 * observer un appel d'offres en brouillon, clôturé, attribué ou annulé
 * via ce endpoint (cf. doc. métier « Consultation des appels d'offres »).
 */
@Service
public class ListAppelOffresPubliesUseCaseImpl implements ListAppelOffresPubliesUseCase {

    private final AppelOffreRepositoryPort appelOffreRepositoryPort;
    private final AppelOffreDetailAssembler appelOffreDetailAssembler;

    public ListAppelOffresPubliesUseCaseImpl(AppelOffreRepositoryPort appelOffreRepositoryPort,
            AppelOffreDetailAssembler appelOffreDetailAssembler) {
        this.appelOffreRepositoryPort = appelOffreRepositoryPort;
        this.appelOffreDetailAssembler = appelOffreDetailAssembler;
    }

    @Override
    @Transactional(readOnly = true)
    public AppelOffrePage lister(ListAppelOffresQuery query) {
        AppelOffreSearchCriteria criteria = new AppelOffreSearchCriteria(query.recherche(),
                StatutAppelOffre.PUBLIE);
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
