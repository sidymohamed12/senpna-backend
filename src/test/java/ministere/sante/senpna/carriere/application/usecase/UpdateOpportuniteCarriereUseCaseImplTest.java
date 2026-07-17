package ministere.sante.senpna.carriere.application.usecase;

import ministere.sante.senpna.carriere.application.service.OpportuniteCarriereCommandMapper;
import ministere.sante.senpna.carriere.application.service.OpportuniteCarriereDetailAssembler;
import ministere.sante.senpna.carriere.domain.command.OpportuniteCarriereCommands.UpdateOpportuniteCarriereCommand;
import ministere.sante.senpna.carriere.domain.exception.OpportuniteCarriereIntrouvableException;
import ministere.sante.senpna.carriere.domain.model.OpportuniteCarriere;
import ministere.sante.senpna.carriere.domain.port.out.OpportuniteCarriereRepositoryPort;
import ministere.sante.senpna.carriere.domain.valueobject.OpportuniteCarriereId;
import ministere.sante.senpna.carriere.domain.valueobject.TypeContrat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
@DisplayName("UpdateOpportuniteCarriereUseCaseImpl — modification d'une opportunité de carrière")
class UpdateOpportuniteCarriereUseCaseImplTest {

    @Mock
    OpportuniteCarriereRepositoryPort opportuniteCarriereRepositoryPort;
    @Mock
    OpportuniteCarriereCommandMapper commandMapper;
    @Mock
    OpportuniteCarriereDetailAssembler assembler;

    UpdateOpportuniteCarriereUseCaseImpl sut;

    @BeforeEach
    void setUp() {
        sut = new UpdateOpportuniteCarriereUseCaseImpl(opportuniteCarriereRepositoryPort, commandMapper, assembler);
    }

    @Test
    @DisplayName("opportunité introuvable → OpportuniteCarriereIntrouvableException")
    void introuvable_leveException() {
        UUID id = UUID.randomUUID();
        when(opportuniteCarriereRepositoryPort.findById(OpportuniteCarriereId.of(id))).thenReturn(Optional.empty());

        var updateOpportuniteCarriereCommand = new UpdateOpportuniteCarriereCommand(id, "T", "E", "D", null, "L", "CDI",
                LocalDate.now(), LocalDate.now(), "e@e.sn");
        assertThatThrownBy(() -> sut.modifier(updateOpportuniteCarriereCommand))
                .isInstanceOf(OpportuniteCarriereIntrouvableException.class);
    }

    @Test
    @DisplayName("modification valide → contenu mis à jour et sauvegardé")
    void modificationValide_metAJourEtSauvegarde() {
        UUID id = UUID.randomUUID();
        OpportuniteCarriere opportunite = OpportuniteCarriere.creer(new OpportuniteCarriere.CreationCommand("Ancien titre", "Entreprise", "Desc", null,
                "Dakar", TypeContrat.CDD, LocalDate.now().plusMonths(1), LocalDate.now().plusDays(10),
                UUID.randomUUID(), "Auteur", null));
        when(opportuniteCarriereRepositoryPort.findById(OpportuniteCarriereId.of(id)))
                .thenReturn(Optional.of(opportunite));
        when(commandMapper.versTypeContrat("CDI")).thenReturn(TypeContrat.CDI);
        when(opportuniteCarriereRepositoryPort.save(opportunite)).thenReturn(opportunite);

        sut.modifier(new UpdateOpportuniteCarriereCommand(id, "Nouveau titre", "Entreprise", "Desc", null, "Dakar",
                "CDI", LocalDate.now().plusMonths(1), LocalDate.now().plusDays(20), "e@e.sn"));

        assertThat(opportunite.getTitre()).isEqualTo("Nouveau titre");
        assertThat(opportunite.getTypeContrat()).isEqualTo(TypeContrat.CDI);
    }
}
