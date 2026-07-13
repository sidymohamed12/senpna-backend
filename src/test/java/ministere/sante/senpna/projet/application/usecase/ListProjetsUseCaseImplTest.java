package ministere.sante.senpna.projet.application.usecase;

import ministere.sante.senpna.projet.application.service.ProjetCommandMapper;
import ministere.sante.senpna.projet.application.service.ProjetDetailAssembler;
import ministere.sante.senpna.projet.domain.command.ProjetCommands.ListProjetsQuery;
import ministere.sante.senpna.projet.domain.command.ProjetCommands.ProjetDetail;
import ministere.sante.senpna.projet.domain.command.ProjetCommands.ProjetPage;
import ministere.sante.senpna.projet.domain.criteria.ProjetSearchCriteria;
import ministere.sante.senpna.projet.domain.model.Projet;
import ministere.sante.senpna.projet.domain.port.out.ProjetRepositoryPort;
import ministere.sante.senpna.projet.domain.valueobject.CategorieProjet;
import ministere.sante.senpna.projet.domain.valueobject.StatutProjet;
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
@DisplayName("ListProjetsUseCaseImpl — recherche paginée des projets")
class ListProjetsUseCaseImplTest {

    @Mock
    ProjetRepositoryPort projetRepositoryPort;
    @Mock
    ProjetCommandMapper commandMapper;
    @Mock
    ProjetDetailAssembler assembler;

    ListProjetsUseCaseImpl sut;

    @BeforeEach
    void setUp() {
        sut = new ListProjetsUseCaseImpl(projetRepositoryPort, commandMapper, assembler);
    }

    @Test
    @DisplayName("convertit catégorie et statut via le mapper, mappe chaque résultat")
    void convertitEtMappeResultats() {
        when(commandMapper.versCategorieOptionnelle("SANTE")).thenReturn(CategorieProjet.SANTE);
        when(commandMapper.versStatutOptionnel("PUBLIE")).thenReturn(StatutProjet.PUBLIE);
        Projet projet = Projet.creer(CategorieProjet.SANTE, "Nom", "Desc", List.of(), List.of(), null);
        when(projetRepositoryPort.search(eq(new ProjetSearchCriteria(null, CategorieProjet.SANTE,
                StatutProjet.PUBLIE)), any())).thenReturn(PageResult.of(List.of(projet), 0, 20, 1));
        ProjetDetail detail = new ProjetDetail(null, "SANTE", "Nom", null, List.of(), List.of(), null, "PUBLIE",
                null, null);
        when(assembler.assembler(projet)).thenReturn(detail);

        ProjetPage page = sut.lister(new ListProjetsQuery(null, "SANTE", "PUBLIE", 0, 20, null, null));

        assertThat(page.content()).containsExactly(detail);
    }

    @Test
    @DisplayName("catégorie et statut absents → critères null, aucun filtre")
    void categorieEtStatutAbsents_criteresNull() {
        when(commandMapper.versCategorieOptionnelle(null)).thenReturn(null);
        when(commandMapper.versStatutOptionnel(null)).thenReturn(null);
        when(projetRepositoryPort.search(eq(ProjetSearchCriteria.vide()), any()))
                .thenReturn(PageResult.of(List.of(), 0, 20, 0));

        ProjetPage page = sut.lister(new ListProjetsQuery(null, null, null, null, null, null, null));

        assertThat(page.content()).isEmpty();
    }
}
