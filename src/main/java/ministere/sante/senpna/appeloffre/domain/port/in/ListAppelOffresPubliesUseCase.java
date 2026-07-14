package ministere.sante.senpna.appeloffre.domain.port.in;

import ministere.sante.senpna.appeloffre.domain.command.AppelOffreCommands.AppelOffrePage;
import ministere.sante.senpna.appeloffre.domain.command.AppelOffreCommands.ListAppelOffresQuery;

/**
 * Consultation, côté espace fournisseur, des appels d'offres ouverts à la
 * soumission. Séparé de {@link ListAppelOffresUseCase} (réservé à la PNA,
 * qui voit tous les statuts) pour matérialiser explicitement la règle
 * métier « un fournisseur ne voit que les AO publiés ».
 */
public interface ListAppelOffresPubliesUseCase {
    AppelOffrePage lister(ListAppelOffresQuery query);
}
