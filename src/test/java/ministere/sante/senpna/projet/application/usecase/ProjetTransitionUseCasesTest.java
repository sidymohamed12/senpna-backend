package ministere.sante.senpna.projet.application.usecase;

import ministere.sante.senpna.projet.application.service.ProjetDetailAssembler;
import ministere.sante.senpna.projet.domain.command.ProjetCommands.ArchiverProjetCommand;
import ministere.sante.senpna.projet.domain.command.ProjetCommands.DesactiverProjetCommand;
import ministere.sante.senpna.projet.domain.command.ProjetCommands.PublierProjetCommand;
import ministere.sante.senpna.projet.domain.command.ProjetCommands.RemettreEnBrouillonProjetCommand;
import ministere.sante.senpna.projet.domain.exception.ProjetIntrouvableException;
import ministere.sante.senpna.projet.domain.model.Projet;
import ministere.sante.senpna.projet.domain.port.out.ProjetRepositoryPort;
import ministere.sante.senpna.projet.domain.valueobject.ProjetId;
import ministere.sante.senpna.projet.domain.valueobject.StatutProjet;

import ministere.sante.senpna.projet.domain.valueobject.CategorieProjet;

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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Use cases de transition de statut d'un projet")
class ProjetTransitionUseCasesTest {

    @Mock
    ProjetRepositoryPort projetRepositoryPort;
    @Mock
    ProjetDetailAssembler assembler;

    UUID id;
    Projet projet;

    @BeforeEach
    void setUp() {
        projet = Projet.creer(new Projet.CreationCommand(CategorieProjet.SANTE, "Nom", "Desc", List.of("O"), List.of("I"), null));
        id = projet.getId().getValue();
    }

    @Nested
    @DisplayName("PublierProjetUseCaseImpl")
    class Publier {

        @Test
        @DisplayName("publie le projet et le sauvegarde")
        void publieEtSauvegarde() {
            when(projetRepositoryPort.findById(ProjetId.of(id)))
                    .thenReturn(Optional.of(projet));

            when(projetRepositoryPort.save(projet))
                    .thenReturn(projet);

            when(assembler.assembler(any()))
                    .thenReturn(null);

            new PublierProjetUseCaseImpl(projetRepositoryPort, assembler)
                    .publier(new PublierProjetCommand(id));

            assertThat(projet.getStatut())
                    .isEqualTo(StatutProjet.PUBLIE);
        }

        @Test
        @DisplayName("projet introuvable → ProjetIntrouvableException")
        void introuvable_leveException() {
            when(projetRepositoryPort.findById(ProjetId.of(id))).thenReturn(Optional.empty());

            var sut = new PublierProjetUseCaseImpl(projetRepositoryPort, assembler);
            var command = new PublierProjetCommand(id);
            assertThatThrownBy(() -> sut
                    .publier(command))
                    .isInstanceOf(ProjetIntrouvableException.class);
        }
    }

    @Nested
    @DisplayName("ArchiverProjetUseCaseImpl")
    class Archiver {

        @Test
        @DisplayName("archive le projet et le sauvegarde")
        void archiveEtSauvegarde() {
            when(projetRepositoryPort.findById(ProjetId.of(id))).thenReturn(Optional.of(projet));
            when(projetRepositoryPort.save(projet)).thenReturn(projet);

            new ArchiverProjetUseCaseImpl(projetRepositoryPort, assembler).archiver(new ArchiverProjetCommand(id));

            assertThat(projet.getStatut()).isEqualTo(StatutProjet.ARCHIVE);
        }

        @Test
        @DisplayName("projet introuvable → ProjetIntrouvableException")
        void introuvable_leveException() {
            when(projetRepositoryPort.findById(ProjetId.of(id))).thenReturn(Optional.empty());

            var sut = new ArchiverProjetUseCaseImpl(projetRepositoryPort, assembler);
            var command = new ArchiverProjetCommand(id);
            assertThatThrownBy(() -> sut.archiver(command))
                    .isInstanceOf(ProjetIntrouvableException.class);
        }
    }

    @Nested
    @DisplayName("DesactiverProjetUseCaseImpl")
    class Desactiver {

        @Test
        @DisplayName("désactive le projet et le sauvegarde")
        void desactiveEtSauvegarde() {
            when(projetRepositoryPort.findById(ProjetId.of(id))).thenReturn(Optional.of(projet));
            when(projetRepositoryPort.save(projet)).thenReturn(projet);

            new DesactiverProjetUseCaseImpl(projetRepositoryPort, assembler)
                    .desactiver(new DesactiverProjetCommand(id));

            assertThat(projet.getStatut()).isEqualTo(StatutProjet.DESACTIVE);
        }

        @Test
        @DisplayName("projet introuvable → ProjetIntrouvableException")
        void introuvable_leveException() {
            when(projetRepositoryPort.findById(ProjetId.of(id))).thenReturn(Optional.empty());

            var sut = new DesactiverProjetUseCaseImpl(projetRepositoryPort, assembler);
            var command = new DesactiverProjetCommand(id);
            assertThatThrownBy(() -> sut.desactiver(command))
                    .isInstanceOf(ProjetIntrouvableException.class);
        }
    }

    @Nested
    @DisplayName("RemettreEnBrouillonProjetUseCaseImpl")
    class RemettreEnBrouillon {

        @Test
        @DisplayName("remet le projet publié en brouillon et le sauvegarde")
        void remetEnBrouillonEtSauvegarde() {
            projet.publier();
            when(projetRepositoryPort.findById(ProjetId.of(id))).thenReturn(Optional.of(projet));
            when(projetRepositoryPort.save(projet)).thenReturn(projet);

            new RemettreEnBrouillonProjetUseCaseImpl(projetRepositoryPort, assembler)
                    .remettreEnBrouillon(new RemettreEnBrouillonProjetCommand(id));

            assertThat(projet.getStatut()).isEqualTo(StatutProjet.BROUILLON);
        }

        @Test
        @DisplayName("projet introuvable → ProjetIntrouvableException")
        void introuvable_leveException() {
            when(projetRepositoryPort.findById(ProjetId.of(id))).thenReturn(Optional.empty());

            var sut = new RemettreEnBrouillonProjetUseCaseImpl(projetRepositoryPort, assembler);
            var command = new RemettreEnBrouillonProjetCommand(id);
            assertThatThrownBy(() -> sut.remettreEnBrouillon(command))
                    .isInstanceOf(ProjetIntrouvableException.class);
        }
    }
}