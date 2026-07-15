package ministere.sante.senpna.commandeachat.application.usecase;

import ministere.sante.senpna.commandeachat.application.service.FactureDetailAssembler;
import ministere.sante.senpna.commandeachat.domain.command.FactureCommands.FacturePage;
import ministere.sante.senpna.commandeachat.domain.command.FactureCommands.ListFacturesQuery;
import ministere.sante.senpna.commandeachat.domain.model.Facture;
import ministere.sante.senpna.commandeachat.domain.port.in.ListFacturesUseCase;
import ministere.sante.senpna.commandeachat.domain.port.out.FactureRepositoryPort;
import ministere.sante.senpna.shared.domain.valueobject.PageRequest;
import ministere.sante.senpna.shared.domain.valueobject.PageResult;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ListFacturesUseCaseImpl implements ListFacturesUseCase {

    private final FactureRepositoryPort factureRepositoryPort;
    private final FactureDetailAssembler factureDetailAssembler;

    public ListFacturesUseCaseImpl(FactureRepositoryPort factureRepositoryPort,
            FactureDetailAssembler factureDetailAssembler) {
        this.factureRepositoryPort = factureRepositoryPort;
        this.factureDetailAssembler = factureDetailAssembler;
    }

    @Override
    @Transactional(readOnly = true)
    public FacturePage lister(ListFacturesQuery query) {
        PageRequest pageRequest = PageRequest.of(query.page(), query.size(), null, null);

        PageResult<Facture> result = factureRepositoryPort.findAll(query.statut(), pageRequest);

        return new FacturePage(
                result.content().stream().map(factureDetailAssembler::assembler).toList(),
                result.page(),
                result.size(),
                result.totalElements(),
                result.totalPages());
    }
}
