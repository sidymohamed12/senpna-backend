package ministere.sante.senpna.catalogue.domain.port.in;

import ministere.sante.senpna.catalogue.domain.command.CatalogueCommands.CataloguePage;
import ministere.sante.senpna.catalogue.domain.command.CatalogueCommands.ConsulterCatalogueRegionalQuery;

/**
 * Consultation du catalogue régional (stock de la PRA d'une région,
 * agrégé par médicament) — visible uniquement par les structures
 * sanitaires appartenant à cette région (une structure ne peut jamais
 * consulter le catalogue d'une autre région que la sienne).
 */
public interface ConsulterCatalogueRegionalUseCase {

    CataloguePage consulter(ConsulterCatalogueRegionalQuery query);
}
