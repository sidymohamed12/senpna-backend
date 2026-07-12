package ministere.sante.senpna.catalogue.application.facade;

import ministere.sante.senpna.catalogue.domain.command.CatalogueCommands.CatalogueInterPraPage;
import ministere.sante.senpna.catalogue.domain.command.CatalogueCommands.CataloguePage;
import ministere.sante.senpna.catalogue.domain.command.CatalogueCommands.ConsulterCatalogueInterPraQuery;
import ministere.sante.senpna.catalogue.domain.command.CatalogueCommands.ConsulterCatalogueNationalQuery;
import ministere.sante.senpna.catalogue.domain.command.CatalogueCommands.ConsulterCatalogueRegionalQuery;
import ministere.sante.senpna.catalogue.domain.port.in.ConsulterCatalogueInterPraUseCase;
import ministere.sante.senpna.catalogue.domain.port.in.ConsulterCatalogueNationalUseCase;
import ministere.sante.senpna.catalogue.domain.port.in.ConsulterCatalogueRegionalUseCase;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CatalogueFacade — délégation aux use cases de consultation")
class CatalogueFacadeTest {

    @Mock
    ConsulterCatalogueNationalUseCase consulterCatalogueNationalUseCase;
    @Mock
    ConsulterCatalogueInterPraUseCase consulterCatalogueInterPraUseCase;
    @Mock
    ConsulterCatalogueRegionalUseCase consulterCatalogueRegionalUseCase;

    CatalogueFacade sut;

    @BeforeEach
    void setUp() {
        sut = new CatalogueFacade(consulterCatalogueNationalUseCase, consulterCatalogueInterPraUseCase,
                consulterCatalogueRegionalUseCase);
    }

    @Test
    @DisplayName("consulterCatalogueNational() délègue au use case national et renvoie son résultat")
    void consulterCatalogueNational_delegue() {
        ConsulterCatalogueNationalQuery query = new ConsulterCatalogueNationalQuery(null, null, null, null);
        CataloguePage page = new CataloguePage(List.of(), 0, 20, 0, 0);
        when(consulterCatalogueNationalUseCase.consulter(query)).thenReturn(page);

        CataloguePage resultat = sut.consulterCatalogueNational(query);

        assertThat(resultat).isSameAs(page);
    }

    @Test
    @DisplayName("consulterCatalogueInterPra() délègue au use case inter-PRA et renvoie son résultat")
    void consulterCatalogueInterPra_delegue() {
        ConsulterCatalogueInterPraQuery query = new ConsulterCatalogueInterPraQuery(null, null, null, null, null);
        CatalogueInterPraPage page = new CatalogueInterPraPage(List.of(), 0, 20, 0, 0);
        when(consulterCatalogueInterPraUseCase.consulter(query)).thenReturn(page);

        CatalogueInterPraPage resultat = sut.consulterCatalogueInterPra(query);

        assertThat(resultat).isSameAs(page);
    }

    @Test
    @DisplayName("consulterCatalogueRegional() délègue au use case régional et renvoie son résultat")
    void consulterCatalogueRegional_delegue() {
        ConsulterCatalogueRegionalQuery query = new ConsulterCatalogueRegionalQuery(UUID.randomUUID(), null, null,
                null, null);
        CataloguePage page = new CataloguePage(List.of(), 0, 20, 0, 0);
        when(consulterCatalogueRegionalUseCase.consulter(query)).thenReturn(page);

        CataloguePage resultat = sut.consulterCatalogueRegional(query);

        assertThat(resultat).isSameAs(page);
    }
}
