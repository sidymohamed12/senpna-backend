package ministere.sante.senpna.organisation.domain.port.in.affectation;

import ministere.sante.senpna.organisation.domain.command.OrganisationCommands.RejectAdhesionCommand;
import ministere.sante.senpna.organisation.domain.command.OrganisationCommands.StructureSanitaireDetail;

public interface RejectAdhesionUseCase {
    StructureSanitaireDetail rejeter(RejectAdhesionCommand command);
}
