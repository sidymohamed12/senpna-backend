package ministere.sante.senpna.medicament.domain.port.in.famille;

import ministere.sante.senpna.medicament.domain.command.FamilleCommands.DesarchiveFamilleCommand;
import ministere.sante.senpna.medicament.domain.command.FamilleCommands.FamilleDetail;

public interface DesarchiveFamilleUseCase {
    FamilleDetail desarchiver(DesarchiveFamilleCommand command);
}
