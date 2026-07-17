package ministere.sante.senpna.carriere.infrastructure.scheduler;

import ministere.sante.senpna.carriere.domain.model.OpportuniteCarriere;
import ministere.sante.senpna.carriere.domain.port.out.OpportuniteCarriereRepositoryPort;
import ministere.sante.senpna.carriere.domain.valueobject.StatutOpportunite;
import ministere.sante.senpna.carriere.domain.valueobject.TypeContrat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CloturerOpportunitesExpireesJob — clôture automatique planifiée")
class CloturerOpportunitesExpireesJobTest {

    @Mock
    OpportuniteCarriereRepositoryPort opportuniteCarriereRepositoryPort;

    CloturerOpportunitesExpireesJob sut;

    @BeforeEach
    void setUp() {
        sut = new CloturerOpportunitesExpireesJob(opportuniteCarriereRepositoryPort);
    }

    @Test
    @DisplayName("aucune opportunité expirée → aucune sauvegarde")
    void aucuneExpiree_aucuneSauvegarde() {
        when(opportuniteCarriereRepositoryPort.findOuvertesExpirees(any())).thenReturn(List.of());

        sut.cloturerLesOpportunitesExpirees();

        verify(opportuniteCarriereRepositoryPort, never()).save(any());
    }

    @Test
    @DisplayName("opportunités expirées trouvées → chacune est clôturée et sauvegardée")
    void expireesTrouvees_chacuneClotureeEtSauvegardee() {
        OpportuniteCarriere o1 = OpportuniteCarriere.creer(new OpportuniteCarriere.CreationCommand("T1", "E", "D", null, "L", TypeContrat.CDI,
                LocalDate.now().plusDays(10), LocalDate.now().plusDays(1), UUID.randomUUID(), "A", null));
        o1.publier();
        OpportuniteCarriere o2 = OpportuniteCarriere.creer(new OpportuniteCarriere.CreationCommand("T2", "E", "D", null, "L", TypeContrat.CDD,
                LocalDate.now().plusDays(10), LocalDate.now().plusDays(1), UUID.randomUUID(), "A", null));
        o2.publier();
        when(opportuniteCarriereRepositoryPort.findOuvertesExpirees(any())).thenReturn(List.of(o1, o2));
        when(opportuniteCarriereRepositoryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));

        sut.cloturerLesOpportunitesExpirees();

        assertThat(o1.getStatut()).isEqualTo(StatutOpportunite.CLOTURE);
        assertThat(o2.getStatut()).isEqualTo(StatutOpportunite.CLOTURE);
        verify(opportuniteCarriereRepositoryPort, times(2)).save(any());
    }
}
