package ministere.sante.senpna.commandeachat.application.usecase;

import ministere.sante.senpna.commandeachat.application.service.FactureDetailAssembler;
import ministere.sante.senpna.commandeachat.domain.command.FactureCommands.FactureDetail;
import ministere.sante.senpna.commandeachat.domain.command.FactureCommands.MarquerFacturePayeeCommand;
import ministere.sante.senpna.commandeachat.domain.exception.FactureIntrouvableException;
import ministere.sante.senpna.commandeachat.domain.model.Facture;
import ministere.sante.senpna.commandeachat.domain.port.in.MarquerFacturePayeeUseCase;
import ministere.sante.senpna.commandeachat.domain.port.out.FactureRepositoryPort;
import ministere.sante.senpna.commandeachat.domain.valueobject.FactureId;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MarquerFacturePayeeUseCaseImpl implements MarquerFacturePayeeUseCase {

    private final FactureRepositoryPort factureRepositoryPort;
    private final FactureDetailAssembler factureDetailAssembler;

    public MarquerFacturePayeeUseCaseImpl(FactureRepositoryPort factureRepositoryPort, FactureDetailAssembler factureDetailAssembler) {
        this.factureRepositoryPort = factureRepositoryPort;
        this.factureDetailAssembler = factureDetailAssembler;
    }

    @Override
    @Transactional
    public FactureDetail marquerPayee(MarquerFacturePayeeCommand command) {
        Facture facture = factureRepositoryPort.findById(FactureId.of(command.factureId()))
                .orElseThrow(FactureIntrouvableException::new);

        facture.marquerPayee();

        Facture saved = factureRepositoryPort.save(facture);
        return factureDetailAssembler.assembler(saved);
    }
}
