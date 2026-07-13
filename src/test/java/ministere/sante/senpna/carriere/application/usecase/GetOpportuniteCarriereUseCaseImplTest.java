package ministere.sante.senpna.carriere.application.usecase;

import ministere.sante.senpna.carriere.application.service.OpportuniteCarriereDetailAssembler;
import ministere.sante.senpna.carriere.domain.command.OpportuniteCarriereCommands.GetOpportuniteCarriereQuery;
import ministere.sante.senpna.carriere.domain.command.OpportuniteCarriereCommands.OpportuniteCarriereDetail;
import ministere.sante.senpna.carriere.domain.exception.OpportuniteCarriereIntrouvableException;
import ministere.sante.senpna.carriere.domain.model.OpportuniteCarriere;
import ministere.sante.senpna.carriere.domain.port.out.OpportuniteCarriereRepositoryPort;
import ministere.sante.senpna.carriere.domain.valueobject.OpportuniteCarriereId;
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
@DisplayName("GetOpportuniteCarriereUseCaseImpl / GetOpportuniteCarrierePubliqueUseCaseImpl")
class GetOpportuniteCarriereUseCaseImplTest {

    @Mock
    OpportuniteCarriereRepositoryPort opportuniteCarriereRepositoryPort;
    @Mock
    OpportuniteCarriereDetailAssembler assembler;

    private OpportuniteCarriere opportunite(boolean publiee) {
        OpportuniteCarriere o = OpportuniteCarriere.creer("Titre", "Entreprise", "Desc", null, "Dakar",
                TypeContrat.CDI, LocalDate.now().plusMonths(2), LocalDate.now().plusMonths(1), UUID.randomUUID(),
                "Auteur", null);
        if (publiee) {
            o.publier();
        }
        return o;
    }

    @Nested
    @DisplayName("GetOpportuniteCarriereUseCaseImpl (vue interne)")
    class Interne {

        GetOpportuniteCarriereUseCaseImpl sut;

        @BeforeEach
        void setUp() {
            sut = new GetOpportuniteCarriereUseCaseImpl(opportuniteCarriereRepositoryPort, assembler);
        }

        @Test
        @DisplayName("renvoie l'opportunité quel que soit son statut")
        void renvoieMemeSiBrouillon() {
            UUID id = UUID.randomUUID();
            OpportuniteCarriere o = opportunite(false);
            when(opportuniteCarriereRepositoryPort.findById(OpportuniteCarriereId.of(id)))
                    .thenReturn(Optional.of(o));
            OpportuniteCarriereDetail detail = new OpportuniteCarriereDetail(id, "T", "E", null, null, "L", "CDI",
                    null, null, null, null, null, "BROUILLON", null, null);
            when(assembler.assembler(o)).thenReturn(detail);

            assertThat(sut.obtenir(new GetOpportuniteCarriereQuery(id))).isSameAs(detail);
        }

        @Test
        @DisplayName("introuvable → OpportuniteCarriereIntrouvableException")
        void introuvable_leveException() {
            UUID id = UUID.randomUUID();
            when(opportuniteCarriereRepositoryPort.findById(OpportuniteCarriereId.of(id)))
                    .thenReturn(Optional.empty());

            var getOpportuniteCarriereQuery = new GetOpportuniteCarriereQuery(id);
            assertThatThrownBy(() -> sut.obtenir(getOpportuniteCarriereQuery))
                    .isInstanceOf(OpportuniteCarriereIntrouvableException.class);
        }
    }

    @Nested
    @DisplayName("GetOpportuniteCarrierePubliqueUseCaseImpl (vue publique)")
    class Publique {

        GetOpportuniteCarrierePubliqueUseCaseImpl sut;

        @BeforeEach
        void setUp() {
            sut = new GetOpportuniteCarrierePubliqueUseCaseImpl(opportuniteCarriereRepositoryPort, assembler);
        }

        @Test
        @DisplayName("opportunité OUVERTE → visible et renvoyée")
        void ouverte_renvoyee() {
            UUID id = UUID.randomUUID();
            OpportuniteCarriere o = opportunite(true);
            when(opportuniteCarriereRepositoryPort.findById(OpportuniteCarriereId.of(id)))
                    .thenReturn(Optional.of(o));
            OpportuniteCarriereDetail detail = new OpportuniteCarriereDetail(id, "T", "E", null, null, "L", "CDI",
                    null, null, null, null, null, "OUVERT", null, null);
            when(assembler.assembler(o)).thenReturn(detail);

            assertThat(sut.obtenirPublique(new GetOpportuniteCarriereQuery(id))).isSameAs(detail);
        }

        @Test
        @DisplayName("opportunité en BROUILLON → masquée du public")
        void brouillon_masquee() {
            UUID id = UUID.randomUUID();
            when(opportuniteCarriereRepositoryPort.findById(OpportuniteCarriereId.of(id)))
                    .thenReturn(Optional.of(opportunite(false)));

            var getOpportuniteCarriereQuery = new GetOpportuniteCarriereQuery(id);
            assertThatThrownBy(() -> sut.obtenirPublique(getOpportuniteCarriereQuery))
                    .isInstanceOf(OpportuniteCarriereIntrouvableException.class);
        }
    }
}
