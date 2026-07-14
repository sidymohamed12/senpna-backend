package ministere.sante.senpna.appeloffre.application.facade;

import ministere.sante.senpna.appeloffre.domain.command.AppelOffreCommands.AnnulerAppelOffreCommand;
import ministere.sante.senpna.appeloffre.domain.command.AppelOffreCommands.AppelOffreDetail;
import ministere.sante.senpna.appeloffre.domain.command.AppelOffreCommands.AppelOffrePage;
import ministere.sante.senpna.appeloffre.domain.command.AppelOffreCommands.AttribuerAppelOffreCommand;
import ministere.sante.senpna.appeloffre.domain.command.AppelOffreCommands.ClorerAppelOffreCommand;
import ministere.sante.senpna.appeloffre.domain.command.AppelOffreCommands.CreateAppelOffreCommand;
import ministere.sante.senpna.appeloffre.domain.command.AppelOffreCommands.GetAppelOffreQuery;
import ministere.sante.senpna.appeloffre.domain.command.AppelOffreCommands.ListAppelOffresQuery;
import ministere.sante.senpna.appeloffre.domain.command.AppelOffreCommands.PublierAppelOffreCommand;
import ministere.sante.senpna.appeloffre.domain.command.OffreFournisseurCommands.ListOffresAppelOffreQuery;
import ministere.sante.senpna.appeloffre.domain.command.OffreFournisseurCommands.OffrePage;
import ministere.sante.senpna.appeloffre.domain.command.OffreFournisseurCommands.RejeterOffreCommand;
import ministere.sante.senpna.appeloffre.domain.command.OffreFournisseurCommands.OffreDetail;
import ministere.sante.senpna.appeloffre.domain.command.OffreFournisseurCommands.RetenirOffreCommand;
import ministere.sante.senpna.appeloffre.domain.port.in.AnnulerAppelOffreUseCase;
import ministere.sante.senpna.appeloffre.domain.port.in.AttribuerAppelOffreUseCase;
import ministere.sante.senpna.appeloffre.domain.port.in.ClorerAppelOffreUseCase;
import ministere.sante.senpna.appeloffre.domain.port.in.CreateAppelOffreUseCase;
import ministere.sante.senpna.appeloffre.domain.port.in.GetAppelOffreUseCase;
import ministere.sante.senpna.appeloffre.domain.port.in.ListAppelOffresUseCase;
import ministere.sante.senpna.appeloffre.domain.port.in.ListOffresAppelOffreUseCase;
import ministere.sante.senpna.appeloffre.domain.port.in.PublierAppelOffreUseCase;
import ministere.sante.senpna.appeloffre.domain.port.in.RejeterOffreUseCase;
import ministere.sante.senpna.appeloffre.domain.port.in.RetenirOffreUseCase;

import org.springframework.stereotype.Component;

/**
 * Façade côté PNA — gestion complète des appels d'offres et analyse des
 * offres reçues. Cf. {@link EspaceFournisseurAppelOffreFacade} pour le
 * pendant côté espace fournisseur.
 */
@Component
public class AppelOffreFacade {

    private final CreateAppelOffreUseCase createAppelOffreUseCase;
    private final PublierAppelOffreUseCase publierAppelOffreUseCase;
    private final ClorerAppelOffreUseCase clorerAppelOffreUseCase;
    private final AnnulerAppelOffreUseCase annulerAppelOffreUseCase;
    private final AttribuerAppelOffreUseCase attribuerAppelOffreUseCase;
    private final GetAppelOffreUseCase getAppelOffreUseCase;
    private final ListAppelOffresUseCase listAppelOffresUseCase;
    private final ListOffresAppelOffreUseCase listOffresAppelOffreUseCase;
    private final RetenirOffreUseCase retenirOffreUseCase;
    private final RejeterOffreUseCase rejeterOffreUseCase;

    public AppelOffreFacade(
            CreateAppelOffreUseCase createAppelOffreUseCase,
            PublierAppelOffreUseCase publierAppelOffreUseCase,
            ClorerAppelOffreUseCase clorerAppelOffreUseCase,
            AnnulerAppelOffreUseCase annulerAppelOffreUseCase,
            AttribuerAppelOffreUseCase attribuerAppelOffreUseCase,
            GetAppelOffreUseCase getAppelOffreUseCase,
            ListAppelOffresUseCase listAppelOffresUseCase,
            ListOffresAppelOffreUseCase listOffresAppelOffreUseCase,
            RetenirOffreUseCase retenirOffreUseCase,
            RejeterOffreUseCase rejeterOffreUseCase) {
        this.createAppelOffreUseCase = createAppelOffreUseCase;
        this.publierAppelOffreUseCase = publierAppelOffreUseCase;
        this.clorerAppelOffreUseCase = clorerAppelOffreUseCase;
        this.annulerAppelOffreUseCase = annulerAppelOffreUseCase;
        this.attribuerAppelOffreUseCase = attribuerAppelOffreUseCase;
        this.getAppelOffreUseCase = getAppelOffreUseCase;
        this.listAppelOffresUseCase = listAppelOffresUseCase;
        this.listOffresAppelOffreUseCase = listOffresAppelOffreUseCase;
        this.retenirOffreUseCase = retenirOffreUseCase;
        this.rejeterOffreUseCase = rejeterOffreUseCase;
    }

    public AppelOffreDetail creer(CreateAppelOffreCommand command) {
        return createAppelOffreUseCase.creer(command);
    }

    public AppelOffreDetail publier(PublierAppelOffreCommand command) {
        return publierAppelOffreUseCase.publier(command);
    }

    public AppelOffreDetail clorer(ClorerAppelOffreCommand command) {
        return clorerAppelOffreUseCase.clorer(command);
    }

    public AppelOffreDetail annuler(AnnulerAppelOffreCommand command) {
        return annulerAppelOffreUseCase.annuler(command);
    }

    public AppelOffreDetail attribuer(AttribuerAppelOffreCommand command) {
        return attribuerAppelOffreUseCase.attribuer(command);
    }

    public AppelOffreDetail obtenir(GetAppelOffreQuery query) {
        return getAppelOffreUseCase.obtenir(query);
    }

    public AppelOffrePage lister(ListAppelOffresQuery query) {
        return listAppelOffresUseCase.lister(query);
    }

    public OffrePage listerOffres(ListOffresAppelOffreQuery query) {
        return listOffresAppelOffreUseCase.lister(query);
    }

    public OffreDetail retenirOffre(RetenirOffreCommand command) {
        return retenirOffreUseCase.retenir(command);
    }

    public OffreDetail rejeterOffre(RejeterOffreCommand command) {
        return rejeterOffreUseCase.rejeter(command);
    }
}
