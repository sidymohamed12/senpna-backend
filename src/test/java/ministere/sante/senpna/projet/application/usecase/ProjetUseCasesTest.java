package ministere.sante.senpna.projet.application.usecase;

import ministere.sante.senpna.projet.application.service.ProjetCommandMapper;
import ministere.sante.senpna.projet.application.service.ProjetDetailAssembler;
import ministere.sante.senpna.projet.domain.command.ProjetCommands.CreateProjetCommand;
import ministere.sante.senpna.projet.domain.command.ProjetCommands.DesactiverProjetCommand;
import ministere.sante.senpna.projet.domain.command.ProjetCommands.GetProjetQuery;
import ministere.sante.senpna.projet.domain.command.ProjetCommands.ListProjetsQuery;
import ministere.sante.senpna.projet.domain.command.ProjetCommands.ProjetDetail;
import ministere.sante.senpna.projet.domain.command.ProjetCommands.ProjetPage;
import ministere.sante.senpna.projet.domain.command.ProjetCommands.PublierProjetCommand;
import ministere.sante.senpna.projet.domain.command.ProjetCommands.UpdateProjetCommand;
import ministere.sante.senpna.projet.domain.exception.CategorieProjetInvalideException;
import ministere.sante.senpna.projet.domain.exception.ProjetIntrouvableException;
import ministere.sante.senpna.projet.domain.model.Projet;
import ministere.sante.senpna.projet.domain.port.out.ProjetRepositoryPort;
import ministere.sante.senpna.projet.domain.valueobject.CategorieProjet;
import ministere.sante.senpna.projet.domain.valueobject.ProjetId;
import ministere.sante.senpna.projet.domain.valueobject.StatutProjet;
import ministere.sante.senpna.shared.domain.valueobject.PageResult;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("Projet Use Cases — create / update / publier / archiver / desactiver / get / list")
class ProjetUseCasesTest {

    private static final UUID PROJET_ID = UUID.randomUUID();

    private final ProjetCommandMapper commandMapper = new ProjetCommandMapper();
    private final ProjetDetailAssembler assembler = new ProjetDetailAssembler();

    @ExtendWith(MockitoExtension.class)
    @Nested
    @DisplayName("creer")
    class Creer {

        @Mock
        private ProjetRepositoryPort projetRepositoryPort;

        @Test
        @DisplayName("crée le projet en BROUILLON")
        void creeProjet() {
            CreateProjetUseCaseImpl useCase = new CreateProjetUseCaseImpl(projetRepositoryPort, commandMapper,
                    assembler);
            when(projetRepositoryPort.save(any(Projet.class))).thenAnswer(inv -> inv.getArgument(0));

            ProjetDetail result = useCase.creer(new CreateProjetCommand("SANTE", "Accès aux soins", "Description",
                    List.of("Objectif 1"), List.of("Impact 1"), null));

            assertThat(result.nom()).isEqualTo("Accès aux soins");
            assertThat(result.categorie()).isEqualTo("SANTE");
            assertThat(result.statut()).isEqualTo("BROUILLON");
        }

        @Test
        @DisplayName("lève CategorieProjetInvalideException pour une catégorie inconnue")
        void leveExceptionQuandCategorieInvalide() {
            CreateProjetUseCaseImpl useCase = new CreateProjetUseCaseImpl(projetRepositoryPort, commandMapper,
                    assembler);

            assertThatThrownBy(() -> useCase.creer(new CreateProjetCommand("INEXISTANTE", "Nom", null, List.of(),
                    List.of(), null)))
                    .isInstanceOf(CategorieProjetInvalideException.class);

            verify(projetRepositoryPort, never()).save(any());
        }
    }

    @ExtendWith(MockitoExtension.class)
    @Nested
    @DisplayName("modifier")
    class Modifier {

        @Mock
        private ProjetRepositoryPort projetRepositoryPort;

        @Test
        @DisplayName("modifie le projet quand il existe")
        void modifieQuandExiste() {
            UpdateProjetUseCaseImpl useCase = new UpdateProjetUseCaseImpl(projetRepositoryPort, commandMapper,
                    assembler);
            Projet existant = projetExistant();
            when(projetRepositoryPort.findById(ProjetId.of(PROJET_ID))).thenReturn(Optional.of(existant));
            when(projetRepositoryPort.save(any(Projet.class))).thenAnswer(inv -> inv.getArgument(0));

            ProjetDetail result = useCase.modifier(new UpdateProjetCommand(PROJET_ID, "INNOVATION", "Nom modifié",
                    "Description modifiée", List.of(), List.of(), null));

            assertThat(result.nom()).isEqualTo("Nom modifié");
            assertThat(result.categorie()).isEqualTo("INNOVATION");
        }

        @Test
        @DisplayName("lève ProjetIntrouvableException quand le projet n'existe pas")
        void leveIntrouvableQuandInexistant() {
            UpdateProjetUseCaseImpl useCase = new UpdateProjetUseCaseImpl(projetRepositoryPort, commandMapper,
                    assembler);
            when(projetRepositoryPort.findById(ProjetId.of(PROJET_ID))).thenReturn(Optional.empty());

            assertThatThrownBy(() -> useCase.modifier(new UpdateProjetCommand(PROJET_ID, "SANTE", "Nom", null,
                    List.of(), List.of(), null)))
                    .isInstanceOf(ProjetIntrouvableException.class);
        }
    }

    @ExtendWith(MockitoExtension.class)
    @Nested
    @DisplayName("publier / desactiver")
    class PublierDesactiver {

        @Mock
        private ProjetRepositoryPort projetRepositoryPort;

        @Test
        @DisplayName("publie un projet en brouillon")
        void publieProjetBrouillon() {
            PublierProjetUseCaseImpl useCase = new PublierProjetUseCaseImpl(projetRepositoryPort, assembler);
            Projet existant = projetExistant();
            when(projetRepositoryPort.findById(ProjetId.of(PROJET_ID))).thenReturn(Optional.of(existant));
            when(projetRepositoryPort.save(any(Projet.class))).thenAnswer(inv -> inv.getArgument(0));

            ProjetDetail result = useCase.publier(new PublierProjetCommand(PROJET_ID));

            assertThat(result.statut()).isEqualTo("PUBLIE");
        }

        @Test
        @DisplayName("désactive un projet publié")
        void desactiveProjetPublie() {
            DesactiverProjetUseCaseImpl useCase = new DesactiverProjetUseCaseImpl(projetRepositoryPort, assembler);
            Projet existant = projetExistant();
            existant.publier();
            when(projetRepositoryPort.findById(ProjetId.of(PROJET_ID))).thenReturn(Optional.of(existant));
            when(projetRepositoryPort.save(any(Projet.class))).thenAnswer(inv -> inv.getArgument(0));

            ProjetDetail result = useCase.desactiver(new DesactiverProjetCommand(PROJET_ID));

            assertThat(result.statut()).isEqualTo("DESACTIVE");
        }

        @Test
        @DisplayName("lève ProjetIntrouvableException quand le projet n'existe pas")
        void leveIntrouvableQuandInexistant() {
            PublierProjetUseCaseImpl useCase = new PublierProjetUseCaseImpl(projetRepositoryPort, assembler);
            when(projetRepositoryPort.findById(ProjetId.of(PROJET_ID))).thenReturn(Optional.empty());

            assertThatThrownBy(() -> useCase.publier(new PublierProjetCommand(PROJET_ID)))
                    .isInstanceOf(ProjetIntrouvableException.class);
        }
    }

    @ExtendWith(MockitoExtension.class)
    @Nested
    @DisplayName("obtenir")
    class Obtenir {

        @Mock
        private ProjetRepositoryPort projetRepositoryPort;

        @Test
        @DisplayName("retourne le projet quand il existe")
        void retourneQuandExiste() {
            GetProjetUseCaseImpl useCase = new GetProjetUseCaseImpl(projetRepositoryPort, assembler);
            Projet existant = projetExistant();
            when(projetRepositoryPort.findById(ProjetId.of(PROJET_ID))).thenReturn(Optional.of(existant));

            ProjetDetail result = useCase.obtenir(new GetProjetQuery(PROJET_ID));

            assertThat(result.id()).isEqualTo(PROJET_ID);
        }

        @Test
        @DisplayName("lève ProjetIntrouvableException quand il n'existe pas")
        void leveIntrouvableQuandInexistant() {
            GetProjetUseCaseImpl useCase = new GetProjetUseCaseImpl(projetRepositoryPort, assembler);
            when(projetRepositoryPort.findById(ProjetId.of(PROJET_ID))).thenReturn(Optional.empty());

            assertThatThrownBy(() -> useCase.obtenir(new GetProjetQuery(PROJET_ID)))
                    .isInstanceOf(ProjetIntrouvableException.class);
        }
    }

    @ExtendWith(MockitoExtension.class)
    @Nested
    @DisplayName("lister")
    class Lister {

        @Mock
        private ProjetRepositoryPort projetRepositoryPort;

        @Test
        @DisplayName("délègue la recherche paginée au repository")
        void delegueRecherchePaginee() {
            ListProjetsUseCaseImpl useCase = new ListProjetsUseCaseImpl(projetRepositoryPort, commandMapper,
                    assembler);
            Projet existant = projetExistant();
            when(projetRepositoryPort.search(any(), any())).thenReturn(PageResult.of(List.of(existant), 0, 20, 1));

            ProjetPage result = useCase
                    .lister(new ListProjetsQuery("acces", "SANTE", "BROUILLON", 0, 20, "createdAt", "DESC"));

            assertThat(result.content()).hasSize(1);
            assertThat(result.totalElements()).isEqualTo(1);
        }
    }

    private Projet projetExistant() {
        Instant maintenant = Instant.now();
        return Projet.reconstruct(ProjetId.of(PROJET_ID), CategorieProjet.SANTE, "Nom", "Description", List.of(),
                List.of(), null, StatutProjet.BROUILLON, maintenant, maintenant);
    }
}
