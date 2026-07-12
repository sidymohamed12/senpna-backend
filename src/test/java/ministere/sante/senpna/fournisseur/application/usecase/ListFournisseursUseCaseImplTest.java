package ministere.sante.senpna.fournisseur.application.usecase;

import ministere.sante.senpna.fournisseur.application.service.FournisseurDetailAssembler;
import ministere.sante.senpna.fournisseur.domain.command.FournisseurCommands.FournisseurDetail;
import ministere.sante.senpna.fournisseur.domain.command.FournisseurCommands.FournisseurPage;
import ministere.sante.senpna.fournisseur.domain.command.FournisseurCommands.ListFournisseursQuery;
import ministere.sante.senpna.fournisseur.domain.criteria.FournisseurSearchCriteria;
import ministere.sante.senpna.fournisseur.domain.model.Fournisseur;
import ministere.sante.senpna.fournisseur.domain.port.out.FournisseurRepositoryPort;
import ministere.sante.senpna.shared.domain.valueobject.PageResult;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ListFournisseursUseCaseImpl — recherche paginée des fournisseurs")
class ListFournisseursUseCaseImplTest {

    @Mock
    FournisseurRepositoryPort fournisseurRepositoryPort;
    @Mock
    FournisseurDetailAssembler fournisseurDetailAssembler;

    ListFournisseursUseCaseImpl sut;

    @BeforeEach
    void setUp() {
        sut = new ListFournisseursUseCaseImpl(fournisseurRepositoryPort, fournisseurDetailAssembler);
    }

    @Test
    @DisplayName("construit les critères à partir de la query et mappe chaque résultat")
    void construitCriteresEtMappeResultats() {
        Fournisseur fournisseur = Fournisseur.creer("Nom", null, null, null, null);
        when(fournisseurRepositoryPort.search(eq(new FournisseurSearchCriteria("pha", true)), any()))
                .thenReturn(PageResult.of(List.of(fournisseur), 0, 20, 1));
        FournisseurDetail detail = new FournisseurDetail(null, "Nom", null, null, null, null, true, null, null);
        when(fournisseurDetailAssembler.assembler(fournisseur)).thenReturn(detail);

        FournisseurPage page = sut.lister(new ListFournisseursQuery("pha", true, 0, 20, null, null));

        assertThat(page.content()).containsExactly(detail);
        assertThat(page.totalElements()).isEqualTo(1);
    }

    @Test
    @DisplayName("aucun résultat → page de contenu vide")
    void aucunResultat_pageVide() {
        when(fournisseurRepositoryPort.search(any(), any())).thenReturn(PageResult.of(List.of(), 0, 20, 0));

        FournisseurPage page = sut.lister(new ListFournisseursQuery(null, null, null, null, null, null));

        assertThat(page.content()).isEmpty();
    }
}
