package ministere.sante.senpna.appeloffre.domain.port.in;

import ministere.sante.senpna.appeloffre.domain.command.AppelOffreCommands.AppelOffreDetail;
import ministere.sante.senpna.appeloffre.domain.command.AppelOffreCommands.GetAppelOffreQuery;

/**
 * Consultation, côté espace fournisseur, du détail d'un appel d'offres —
 * refuse l'accès à un AO encore en {@code BROUILLON} (cf.
 * {@link ListAppelOffresPubliesUseCase}).
 */
public interface GetAppelOffrePublieUseCase {
    AppelOffreDetail obtenir(GetAppelOffreQuery query);
}
