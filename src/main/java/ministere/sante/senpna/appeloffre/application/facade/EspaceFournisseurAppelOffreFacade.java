package ministere.sante.senpna.appeloffre.application.facade;

import ministere.sante.senpna.appeloffre.domain.command.AppelOffreCommands.AppelOffreDetail;
import ministere.sante.senpna.appeloffre.domain.command.AppelOffreCommands.AppelOffrePage;
import ministere.sante.senpna.appeloffre.domain.command.AppelOffreCommands.GetAppelOffreQuery;
import ministere.sante.senpna.appeloffre.domain.command.AppelOffreCommands.ListAppelOffresQuery;
import ministere.sante.senpna.appeloffre.domain.command.OffreFournisseurCommands.GetOffreQuery;
import ministere.sante.senpna.appeloffre.domain.command.OffreFournisseurCommands.ListMesOffresQuery;
import ministere.sante.senpna.appeloffre.domain.command.OffreFournisseurCommands.OffreDetail;
import ministere.sante.senpna.appeloffre.domain.command.OffreFournisseurCommands.OffrePage;
import ministere.sante.senpna.appeloffre.domain.command.OffreFournisseurCommands.RetirerOffreCommand;
import ministere.sante.senpna.appeloffre.domain.command.OffreFournisseurCommands.SoumettreOffreCommand;
import ministere.sante.senpna.appeloffre.domain.port.in.GetAppelOffrePublieUseCase;
import ministere.sante.senpna.appeloffre.domain.port.in.GetOffreUseCase;
import ministere.sante.senpna.appeloffre.domain.port.in.ListAppelOffresPubliesUseCase;
import ministere.sante.senpna.appeloffre.domain.port.in.ListMesOffresUseCase;
import ministere.sante.senpna.appeloffre.domain.port.in.RetirerOffreUseCase;
import ministere.sante.senpna.appeloffre.domain.port.in.SoumettreOffreUseCase;

import org.springframework.stereotype.Component;

/**
 * Façade côté espace fournisseur — consultation des appels d'offres
 * publiés et gestion de ses propres offres. N'expose volontairement
 * aucune opération de la façade PNA ({@link AppelOffreFacade}) : un
 * fournisseur ne crée, ne publie, ne clôture ni n'attribue jamais un
 * appel d'offres.
 */
@Component
public class EspaceFournisseurAppelOffreFacade {

    private final ListAppelOffresPubliesUseCase listAppelOffresPubliesUseCase;
    private final GetAppelOffrePublieUseCase getAppelOffrePublieUseCase;
    private final SoumettreOffreUseCase soumettreOffreUseCase;
    private final RetirerOffreUseCase retirerOffreUseCase;
    private final GetOffreUseCase getOffreUseCase;
    private final ListMesOffresUseCase listMesOffresUseCase;

    public EspaceFournisseurAppelOffreFacade(
            ListAppelOffresPubliesUseCase listAppelOffresPubliesUseCase,
            GetAppelOffrePublieUseCase getAppelOffrePublieUseCase,
            SoumettreOffreUseCase soumettreOffreUseCase,
            RetirerOffreUseCase retirerOffreUseCase,
            GetOffreUseCase getOffreUseCase,
            ListMesOffresUseCase listMesOffresUseCase) {
        this.listAppelOffresPubliesUseCase = listAppelOffresPubliesUseCase;
        this.getAppelOffrePublieUseCase = getAppelOffrePublieUseCase;
        this.soumettreOffreUseCase = soumettreOffreUseCase;
        this.retirerOffreUseCase = retirerOffreUseCase;
        this.getOffreUseCase = getOffreUseCase;
        this.listMesOffresUseCase = listMesOffresUseCase;
    }

    public AppelOffrePage listerAppelsOffresPublies(ListAppelOffresQuery query) {
        return listAppelOffresPubliesUseCase.lister(query);
    }

    public AppelOffreDetail obtenirAppelOffre(GetAppelOffreQuery query) {
        return getAppelOffrePublieUseCase.obtenir(query);
    }

    public OffreDetail soumettreOffre(SoumettreOffreCommand command) {
        return soumettreOffreUseCase.soumettre(command);
    }

    public OffreDetail retirerOffre(RetirerOffreCommand command) {
        return retirerOffreUseCase.retirer(command);
    }

    public OffreDetail obtenirOffre(GetOffreQuery query) {
        return getOffreUseCase.obtenir(query);
    }

    public OffrePage listerMesOffres(ListMesOffresQuery query) {
        return listMesOffresUseCase.lister(query);
    }
}
