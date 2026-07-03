package ministere.sante.senpna.catalogue.application.facade;

import ministere.sante.senpna.catalogue.domain.command.CatalogueCommands.CatalogueInterPraPage;
import ministere.sante.senpna.catalogue.domain.command.CatalogueCommands.CataloguePage;
import ministere.sante.senpna.catalogue.domain.command.CatalogueCommands.ConsulterCatalogueInterPraQuery;
import ministere.sante.senpna.catalogue.domain.command.CatalogueCommands.ConsulterCatalogueNationalQuery;
import ministere.sante.senpna.catalogue.domain.command.CatalogueCommands.ConsulterCatalogueRegionalQuery;
import ministere.sante.senpna.catalogue.domain.port.in.ConsulterCatalogueInterPraUseCase;
import ministere.sante.senpna.catalogue.domain.port.in.ConsulterCatalogueNationalUseCase;
import ministere.sante.senpna.catalogue.domain.port.in.ConsulterCatalogueRegionalUseCase;

import org.springframework.stereotype.Component;

@Component
public class CatalogueFacade {

    private final ConsulterCatalogueNationalUseCase consulterCatalogueNationalUseCase;
    private final ConsulterCatalogueInterPraUseCase consulterCatalogueInterPraUseCase;
    private final ConsulterCatalogueRegionalUseCase consulterCatalogueRegionalUseCase;

    public CatalogueFacade(ConsulterCatalogueNationalUseCase consulterCatalogueNationalUseCase,
            ConsulterCatalogueInterPraUseCase consulterCatalogueInterPraUseCase,
            ConsulterCatalogueRegionalUseCase consulterCatalogueRegionalUseCase) {
        this.consulterCatalogueNationalUseCase = consulterCatalogueNationalUseCase;
        this.consulterCatalogueInterPraUseCase = consulterCatalogueInterPraUseCase;
        this.consulterCatalogueRegionalUseCase = consulterCatalogueRegionalUseCase;
    }

    public CataloguePage consulterCatalogueNational(ConsulterCatalogueNationalQuery query) {
        return consulterCatalogueNationalUseCase.consulter(query);
    }

    public CatalogueInterPraPage consulterCatalogueInterPra(ConsulterCatalogueInterPraQuery query) {
        return consulterCatalogueInterPraUseCase.consulter(query);
    }

    public CataloguePage consulterCatalogueRegional(ConsulterCatalogueRegionalQuery query) {
        return consulterCatalogueRegionalUseCase.consulter(query);
    }
}
