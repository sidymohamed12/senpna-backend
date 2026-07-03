package ministere.sante.senpna.catalogue.domain.port.in;

import ministere.sante.senpna.catalogue.domain.command.CatalogueCommands.CataloguePage;
import ministere.sante.senpna.catalogue.domain.command.CatalogueCommands.ConsulterCatalogueNationalQuery;

/**
 * Consultation du catalogue de la PNA (stock de la PNA centrale, agrégé
 * par médicament) — visible uniquement par les acteurs PRA et PNA (cf.
 * doc. produit « Catalogue »).
 */
public interface ConsulterCatalogueNationalUseCase {

    CataloguePage consulter(ConsulterCatalogueNationalQuery query);
}
