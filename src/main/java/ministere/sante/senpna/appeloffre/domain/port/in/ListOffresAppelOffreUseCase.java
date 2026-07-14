package ministere.sante.senpna.appeloffre.domain.port.in;

import ministere.sante.senpna.appeloffre.domain.command.OffreFournisseurCommands.ListOffresAppelOffreQuery;
import ministere.sante.senpna.appeloffre.domain.command.OffreFournisseurCommands.OffrePage;

/** Réservé PNA — analyse comparative des offres reçues pour un AO donné. */
public interface ListOffresAppelOffreUseCase {
    OffrePage lister(ListOffresAppelOffreQuery query);
}
