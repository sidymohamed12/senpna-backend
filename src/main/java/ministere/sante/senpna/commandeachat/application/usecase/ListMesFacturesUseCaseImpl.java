package ministere.sante.senpna.commandeachat.application.usecase;

import ministere.sante.senpna.commandeachat.application.service.FactureDetailAssembler;
import ministere.sante.senpna.commandeachat.domain.command.FactureCommands.FacturePage;
import ministere.sante.senpna.commandeachat.domain.command.FactureCommands.ListMesFacturesQuery;
import ministere.sante.senpna.commandeachat.domain.model.Facture;
import ministere.sante.senpna.commandeachat.domain.port.in.ListMesFacturesUseCase;
import ministere.sante.senpna.commandeachat.domain.port.out.FactureRepositoryPort;
import ministere.sante.senpna.fournisseur.domain.valueobject.FournisseurId;
import ministere.sante.senpna.shared.domain.valueobject.PageRequest;
import ministere.sante.senpna.shared.domain.valueobject.PageResult;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ListMesFacturesUseCaseImpl implements ListMesFacturesUseCase {

    private final FactureRepositoryPort factureRepositoryPort;
    private final FactureDetailAssembler factureDetailAssembler;

    public ListMesFacturesUseCaseImpl(FactureRepositoryPort factureRepositoryPort,
            FactureDetailAssembler factureDetailAssembler) {
        this.factureRepositoryPort = factureRepositoryPort;
        this.factureDetailAssembler = factureDetailAssembler;
    }

    @Override
    @Transactional(readOnly = true)
    public FacturePage lister(ListMesFacturesQuery query) {
        PageRequest pageRequest = PageRequest.of(query.page(), query.size(), null, null);

        PageResult<Facture> result = factureRepositoryPort.findByFournisseurId(
                FournisseurId.of(query.fournisseurId()), query.statut(), pageRequest);

        return new FacturePage(
                result.content().stream().map(factureDetailAssembler::assembler).toList(),
                result.page(),
                result.size(),
                result.totalElements(),
                result.totalPages());
    }
}
