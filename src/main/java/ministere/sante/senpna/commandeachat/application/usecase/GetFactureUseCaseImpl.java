package ministere.sante.senpna.commandeachat.application.usecase;

import ministere.sante.senpna.commandeachat.application.service.FactureDetailAssembler;
import ministere.sante.senpna.commandeachat.domain.command.FactureCommands.FactureDetail;
import ministere.sante.senpna.commandeachat.domain.command.FactureCommands.GetFactureQuery;
import ministere.sante.senpna.commandeachat.domain.exception.AccesFactureRefuseException;
import ministere.sante.senpna.commandeachat.domain.exception.FactureIntrouvableException;
import ministere.sante.senpna.commandeachat.domain.model.Facture;
import ministere.sante.senpna.commandeachat.domain.port.in.GetFactureUseCase;
import ministere.sante.senpna.commandeachat.domain.port.out.FactureRepositoryPort;
import ministere.sante.senpna.commandeachat.domain.valueobject.FactureId;
import ministere.sante.senpna.fournisseur.domain.valueobject.FournisseurId;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GetFactureUseCaseImpl implements GetFactureUseCase {

    private final FactureRepositoryPort factureRepositoryPort;
    private final FactureDetailAssembler factureDetailAssembler;

    public GetFactureUseCaseImpl(FactureRepositoryPort factureRepositoryPort,
            FactureDetailAssembler factureDetailAssembler) {
        this.factureRepositoryPort = factureRepositoryPort;
        this.factureDetailAssembler = factureDetailAssembler;
    }

    @Override
    @Transactional(readOnly = true)
    public FactureDetail obtenir(GetFactureQuery query) {
        Facture facture = factureRepositoryPort.findById(FactureId.of(query.factureId()))
                .orElseThrow(FactureIntrouvableException::new);

        if (query.fournisseurId() != null && !facture.appartientA(FournisseurId.of(query.fournisseurId()))) {
            throw new AccesFactureRefuseException();
        }

        return factureDetailAssembler.assembler(facture);
    }
}
