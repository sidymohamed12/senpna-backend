package ministere.sante.senpna.catalogue.domain.port.in;

import ministere.sante.senpna.catalogue.domain.command.CatalogueCommands.CatalogueInterPraPage;
import ministere.sante.senpna.catalogue.domain.command.CatalogueCommands.ConsulterCatalogueInterPraQuery;

/**
 * Consultation du catalogue inter-PRA (disponibilités de toutes les PRA
 * actives, ventilées par PRA, pour faciliter les transferts/échanges — cf.
 * doc. flows §CAS 3) — visible par tous les acteurs PRA et PNA.
 */
public interface ConsulterCatalogueInterPraUseCase {

    CatalogueInterPraPage consulter(ConsulterCatalogueInterPraQuery query);
}
