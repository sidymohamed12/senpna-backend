package ministere.sante.senpna.carriere.application.usecase;

import ministere.sante.senpna.carriere.application.service.OpportuniteCarriereDetailAssembler;
import ministere.sante.senpna.carriere.domain.command.OpportuniteCarriereCommands.CloturerOpportuniteCommand;
import ministere.sante.senpna.carriere.domain.command.OpportuniteCarriereCommands.MettreEnCoursOpportuniteCommand;
import ministere.sante.senpna.carriere.domain.command.OpportuniteCarriereCommands.PublierOpportuniteCommand;
import ministere.sante.senpna.carriere.domain.command.OpportuniteCarriereCommands.RemettreEnBrouillonOpportuniteCommand;
import ministere.sante.senpna.carriere.domain.exception.OpportuniteCarriereIntrouvableException;
import ministere.sante.senpna.carriere.domain.model.OpportuniteCarriere;
import ministere.sante.senpna.carriere.domain.port.out.OpportuniteCarriereRepositoryPort;
import ministere.sante.senpna.carriere.domain.valueobject.OpportuniteCarriereId;
import ministere.sante.senpna.carriere.domain.valueobject.StatutOpportunite;
import ministere.sante.senpna.carriere.domain.valueobject.TypeContrat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Use cases de transition de statut d'une opportunité de carrière")
class OpportuniteTransitionUseCasesTest {

    @Mock
    OpportuniteCarriereRepositoryPort opportuniteCarriereRepositoryPort;
    @Mock
    OpportuniteCarriereDetailAssembler assembler;

    UUID id;
    OpportuniteCarriere opportunite;

    @BeforeEach
    void setUp() {
        id = UUID.randomUUID();
        opportunite = OpportuniteCarriere.creer(new OpportuniteCarriere.CreationCommand("Titre", "Entreprise", "Desc", null, "Dakar", TypeContrat.CDI,
                LocalDate.now().plusMonths(2), LocalDate.now().plusMonths(1), UUID.randomUUID(), "Auteur", null));
    }

    @Nested
    @DisplayName("PublierOpportuniteUseCaseImpl")
    class Publier {

        @Test
        @DisplayName("publie l'opportunité et la sauvegarde")
        void publieEtSauvegarde() {
            when(opportuniteCarriereRepositoryPort.findById(OpportuniteCarriereId.of(id)))
                    .thenReturn(Optional.of(opportunite));
            when(opportuniteCarriereRepositoryPort.save(opportunite)).thenReturn(opportunite);

            new PublierOpportuniteUseCaseImpl(opportuniteCarriereRepositoryPort, assembler)
                    .publier(new PublierOpportuniteCommand(id));

            assertThat(opportunite.getStatut()).isEqualTo(StatutOpportunite.OUVERT);
        }

        @Test
        @DisplayName("introuvable → OpportuniteCarriereIntrouvableException")
        void introuvable_leveException() {
            when(opportuniteCarriereRepositoryPort.findById(OpportuniteCarriereId.of(id)))
                    .thenReturn(Optional.empty());

            var sut = new PublierOpportuniteUseCaseImpl(opportuniteCarriereRepositoryPort, assembler);
            var publierOpportuniteCommand = new PublierOpportuniteCommand(id);
            assertThatThrownBy(() -> sut
                    .publier(publierOpportuniteCommand))
                    .isInstanceOf(OpportuniteCarriereIntrouvableException.class);
        }
    }

    @Nested
    @DisplayName("MettreEnCoursOpportuniteUseCaseImpl")
    class MettreEnCours {

        @Test
        @DisplayName("passe l'opportunité en EN_COURS et la sauvegarde")
        void passeEnCoursEtSauvegarde() {
            when(opportuniteCarriereRepositoryPort.findById(OpportuniteCarriereId.of(id)))
                    .thenReturn(Optional.of(opportunite));
            when(opportuniteCarriereRepositoryPort.save(opportunite)).thenReturn(opportunite);

            new MettreEnCoursOpportuniteUseCaseImpl(opportuniteCarriereRepositoryPort, assembler)
                    .mettreEnCours(new MettreEnCoursOpportuniteCommand(id));

            assertThat(opportunite.getStatut()).isEqualTo(StatutOpportunite.EN_COURS);
        }
    }

    @Nested
    @DisplayName("CloturerOpportuniteUseCaseImpl")
    class Cloturer {

        @Test
        @DisplayName("clôture l'opportunité et la sauvegarde")
        void clotureEtSauvegarde() {
            when(opportuniteCarriereRepositoryPort.findById(OpportuniteCarriereId.of(id)))
                    .thenReturn(Optional.of(opportunite));
            when(opportuniteCarriereRepositoryPort.save(opportunite)).thenReturn(opportunite);

            new CloturerOpportuniteUseCaseImpl(opportuniteCarriereRepositoryPort, assembler)
                    .cloturer(new CloturerOpportuniteCommand(id));

            assertThat(opportunite.getStatut()).isEqualTo(StatutOpportunite.CLOTURE);
        }
    }

    @Nested
    @DisplayName("RemettreEnBrouillonOpportuniteUseCaseImpl")
    class RemettreEnBrouillon {

        @Test
        @DisplayName("remet l'opportunité publiée en brouillon et la sauvegarde")
        void remetEnBrouillonEtSauvegarde() {
            opportunite.publier();
            when(opportuniteCarriereRepositoryPort.findById(OpportuniteCarriereId.of(id)))
                    .thenReturn(Optional.of(opportunite));
            when(opportuniteCarriereRepositoryPort.save(opportunite)).thenReturn(opportunite);

            new RemettreEnBrouillonOpportuniteUseCaseImpl(opportuniteCarriereRepositoryPort, assembler)
                    .remettreEnBrouillon(new RemettreEnBrouillonOpportuniteCommand(id));

            assertThat(opportunite.getStatut()).isEqualTo(StatutOpportunite.BROUILLON);
        }
    }
}
