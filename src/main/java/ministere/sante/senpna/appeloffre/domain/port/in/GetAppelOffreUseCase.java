package ministere.sante.senpna.appeloffre.domain.port.in;

import ministere.sante.senpna.appeloffre.domain.command.AppelOffreCommands.AppelOffreDetail;
import ministere.sante.senpna.appeloffre.domain.command.AppelOffreCommands.GetAppelOffreQuery;

public interface GetAppelOffreUseCase {
    AppelOffreDetail obtenir(GetAppelOffreQuery query);
}
