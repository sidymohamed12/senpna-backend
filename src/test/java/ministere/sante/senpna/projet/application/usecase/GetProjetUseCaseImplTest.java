package ministere.sante.senpna.projet.application.usecase;

import ministere.sante.senpna.projet.application.service.ProjetDetailAssembler;
import ministere.sante.senpna.projet.domain.command.ProjetCommands.GetProjetQuery;
import ministere.sante.senpna.projet.domain.command.ProjetCommands.ProjetDetail;
import ministere.sante.senpna.projet.domain.exception.ProjetIntrouvableException;
import ministere.sante.senpna.projet.domain.model.Projet;
import ministere.sante.senpna.projet.domain.port.out.ProjetRepositoryPort;
import ministere.sante.senpna.projet.domain.valueobject.CategorieProjet;
import ministere.sante.senpna.projet.domain.valueobject.ProjetId;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetProjetUseCaseImpl / GetProjetPubliqueUseCaseImpl — consultation d'un projet")
class GetProjetUseCaseImplTest {

    @Mock
    ProjetRepositoryPort projetRepositoryPort;
    @Mock
    ProjetDetailAssembler assembler;

    private Projet projet(boolean publie) {
        Projet p = Projet.creer(CategorieProjet.SANTE, "Nom", "Desc", List.of(), List.of(), null);
        if (publie) {
            p.publier();
        }
        return p;
    }

    @Nested
    @DisplayName("GetProjetUseCaseImpl (vue interne)")
    class Interne {

        GetProjetUseCaseImpl sut;

        @BeforeEach
        void setUp() {
            sut = new GetProjetUseCaseImpl(projetRepositoryPort, assembler);
        }

        @Test
        @DisplayName("renvoie le projet quel que soit son statut (brouillon inclus)")
        void renvoieMemeSiBrouillon() {
            UUID id = UUID.randomUUID();
            Projet p = projet(false);
            when(projetRepositoryPort.findById(ProjetId.of(id))).thenReturn(Optional.of(p));
            ProjetDetail detail = new ProjetDetail(id, "SANTE", "Nom", null, List.of(), List.of(), null,
                    "BROUILLON", null, null);
            when(assembler.assembler(p)).thenReturn(detail);

            assertThat(sut.obtenir(new GetProjetQuery(id))).isSameAs(detail);
        }

        @Test
        @DisplayName("projet introuvable → ProjetIntrouvableException")
        void introuvable_leveException() {
            UUID id = UUID.randomUUID();
            when(projetRepositoryPort.findById(ProjetId.of(id))).thenReturn(Optional.empty());

            var getProjetQuery = new GetProjetQuery(id);
            assertThatThrownBy(() -> sut.obtenir(getProjetQuery))
                    .isInstanceOf(ProjetIntrouvableException.class);
        }
    }

    @Nested
    @DisplayName("GetProjetPubliqueUseCaseImpl (vue publique)")
    class Publique {

        GetProjetPubliqueUseCaseImpl sut;

        @BeforeEach
        void setUp() {
            sut = new GetProjetPubliqueUseCaseImpl(projetRepositoryPort, assembler);
        }

        @Test
        @DisplayName("projet PUBLIE → renvoyé")
        void projetPublie_renvoye() {
            UUID id = UUID.randomUUID();
            Projet p = projet(true);
            when(projetRepositoryPort.findById(ProjetId.of(id))).thenReturn(Optional.of(p));
            ProjetDetail detail = new ProjetDetail(id, "SANTE", "Nom", null, List.of(), List.of(), null, "PUBLIE",
                    null, null);
            when(assembler.assembler(p)).thenReturn(detail);

            assertThat(sut.obtenirPublique(new GetProjetQuery(id))).isSameAs(detail);
        }

        @Test
        @DisplayName("projet en BROUILLON → ProjetIntrouvableException (masqué du public)")
        void projetBrouillon_masqueDuPublic() {
            UUID id = UUID.randomUUID();
            when(projetRepositoryPort.findById(ProjetId.of(id))).thenReturn(Optional.of(projet(false)));

            var getProjetQuery = new GetProjetQuery(id);
            assertThatThrownBy(() -> sut.obtenirPublique(getProjetQuery))
                    .isInstanceOf(ProjetIntrouvableException.class);
        }

        @Test
        @DisplayName("projet introuvable → ProjetIntrouvableException")
        void introuvable_leveException() {
            UUID id = UUID.randomUUID();
            when(projetRepositoryPort.findById(ProjetId.of(id))).thenReturn(Optional.empty());

            var getProjetQuery = new GetProjetQuery(id);
            assertThatThrownBy(() -> sut.obtenirPublique(getProjetQuery))
                    .isInstanceOf(ProjetIntrouvableException.class);
        }
    }
}
