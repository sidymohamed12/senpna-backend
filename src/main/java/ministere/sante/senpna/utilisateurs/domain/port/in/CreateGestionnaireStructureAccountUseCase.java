package ministere.sante.senpna.utilisateurs.domain.port.in;

import ministere.sante.senpna.shared.domain.events.AdhesionValideeEvent;

/**
 * Crée automatiquement le compte {@code GESTIONNAIRE_STRUCTURE} d'une
 * structure sanitaire dont l'adhésion vient d'être validée — jamais
 * appelé manuellement (aucun endpoint HTTP), uniquement en réaction à
 * {@link AdhesionValideeEvent} (cf. {@code AdhesionValideeListener}).
 */
public interface CreateGestionnaireStructureAccountUseCase {

    void creer(AdhesionValideeEvent event);
}
