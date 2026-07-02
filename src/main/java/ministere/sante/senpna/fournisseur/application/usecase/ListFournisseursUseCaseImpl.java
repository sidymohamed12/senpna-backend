package ministere.sante.senpna.fournisseur.application.usecase;

import ministere.sante.senpna.fournisseur.application.service.FournisseurDetailAssembler;
import ministere.sante.senpna.fournisseur.domain.command.FournisseurCommands.FournisseurPage;
import ministere.sante.senpna.fournisseur.domain.command.FournisseurCommands.ListFournisseursQuery;
import ministere.sante.senpna.fournisseur.domain.criteria.FournisseurSearchCriteria;
import ministere.sante.senpna.fournisseur.domain.model.Fournisseur;
import ministere.sante.senpna.fournisseur.domain.port.in.ListFournisseursUseCase;
import ministere.sante.senpna.fournisseur.domain.port.out.FournisseurRepositoryPort;
import ministere.sante.senpna.shared.domain.valueobject.PageRequest;
import ministere.sante.senpna.shared.domain.valueobject.PageResult;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ListFournisseursUseCaseImpl implements ListFournisseursUseCase {

    private final FournisseurRepositoryPort fournisseurRepositoryPort;
    private final FournisseurDetailAssembler fournisseurDetailAssembler;

    public ListFournisseursUseCaseImpl(FournisseurRepositoryPort fournisseurRepositoryPort,
            FournisseurDetailAssembler fournisseurDetailAssembler) {
        this.fournisseurRepositoryPort = fournisseurRepositoryPort;
        this.fournisseurDetailAssembler = fournisseurDetailAssembler;
    }

    @Override
    @Transactional(readOnly = true)
    public FournisseurPage lister(ListFournisseursQuery query) {
        FournisseurSearchCriteria criteria = new FournisseurSearchCriteria(query.recherche(), query.actif());
        PageRequest pageRequest = PageRequest.of(query.page(), query.size(), query.sortBy(), query.sortDirection());

        PageResult<Fournisseur> result = fournisseurRepositoryPort.search(criteria, pageRequest);

        return new FournisseurPage(
                result.content().stream().map(fournisseurDetailAssembler::assembler).toList(),
                result.page(),
                result.size(),
                result.totalElements(),
                result.totalPages());
    }
}
