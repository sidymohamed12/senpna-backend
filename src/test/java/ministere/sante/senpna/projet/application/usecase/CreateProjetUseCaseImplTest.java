package ministere.sante.senpna.projet.application.usecase;

import ministere.sante.senpna.projet.application.service.ProjetCommandMapper;
import ministere.sante.senpna.projet.application.service.ProjetDetailAssembler;
import ministere.sante.senpna.projet.domain.command.ProjetCommands.CreateProjetCommand;
import ministere.sante.senpna.projet.domain.command.ProjetCommands.ProjetDetail;
import ministere.sante.senpna.projet.domain.exception.CategorieProjetInvalideException;
import ministere.sante.senpna.projet.domain.port.out.ProjetRepositoryPort;
import ministere.sante.senpna.projet.domain.valueobject.CategorieProjet;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CreateProjetUseCaseImpl — création d'un projet")
class CreateProjetUseCaseImplTest {

    @Mock
    ProjetRepositoryPort projetRepositoryPort;
    @Mock
    ProjetCommandMapper commandMapper;
    @Mock
    ProjetDetailAssembler assembler;

    CreateProjetUseCaseImpl sut;

    @BeforeEach
    void setUp() {
        sut = new CreateProjetUseCaseImpl(projetRepositoryPort, commandMapper, assembler);
    }

    @Test
    @DisplayName("catégorie invalide → l'exception du mapper se propage, aucune sauvegarde")
    void categorieInvalide_sePropage() {
        when(commandMapper.versCategorie("INEXISTANTE"))
                .thenThrow(new CategorieProjetInvalideException("INEXISTANTE"));

        var command = new CreateProjetCommand("INEXISTANTE", "Nom", "Desc", List.of(), List.of(), null);
        assertThatThrownBy(() -> sut.creer(command))
                .isInstanceOf(CategorieProjetInvalideException.class);

        verify(projetRepositoryPort, never()).save(any());
    }

    @Test
    @DisplayName("création réussie → projet créé en statut initial et sauvegardé")
    void creationReussie_sauvegarde() {
        when(commandMapper.versCategorie("SANTE")).thenReturn(CategorieProjet.SANTE);
        when(projetRepositoryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));
        ProjetDetail detail = new ProjetDetail(null, "SANTE", "Nom", "Desc", List.of(), List.of(), null,
                "BROUILLON", null, null);
        when(assembler.assembler(any())).thenReturn(detail);

        ProjetDetail result = sut.creer(
                new CreateProjetCommand("SANTE", "Nom", "Desc", List.of("O1"), List.of("I1"), "img"));

        assertThat(result).isSameAs(detail);
    }
}
