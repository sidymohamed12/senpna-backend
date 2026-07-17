package ministere.sante.senpna.stock.application.usecase.lot;

import ministere.sante.senpna.shared.domain.exception.SenPnaException;
import ministere.sante.senpna.shared.domain.valueobject.PageResult;
import ministere.sante.senpna.stock.application.service.EntrepotScopeGuard;
import ministere.sante.senpna.stock.application.service.LotDetailAssembler;
import ministere.sante.senpna.stock.domain.command.LotCommands.AlertePeremptionQuery;
import ministere.sante.senpna.stock.domain.command.LotCommands.ListLotsQuery;
import ministere.sante.senpna.stock.domain.command.LotCommands.LotPage;
import ministere.sante.senpna.stock.domain.model.Lot;
import ministere.sante.senpna.stock.domain.port.out.LotRepositoryPort;
import ministere.sante.senpna.fournisseur.domain.valueobject.FournisseurId;
import ministere.sante.senpna.medicament.domain.valueobject.MedicamentId;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
@DisplayName("Listes et opérations planifiées sur les lots")
class ListLotsAndPeremptionUseCasesTest {

    @Mock
    LotRepositoryPort lotRepositoryPort;
    @Mock
    LotDetailAssembler lotDetailAssembler;
    @Mock
    EntrepotScopeGuard entrepotScopeGuard;

    @Nested
    @DisplayName("ListLotsUseCaseImpl")
    class ListLots {

        ListLotsUseCaseImpl sut;

        @BeforeEach
        void setUp() {
            sut = new ListLotsUseCaseImpl(lotRepositoryPort, lotDetailAssembler, entrepotScopeGuard);
        }

        @Test
        @DisplayName("statut invalide → ValidationException")
        void statutInvalide_leveException() {
            when(entrepotScopeGuard.entrepotIdPourLecture(any())).thenReturn(null);

            var listLotsQuery = new ListLotsQuery(null, null, null, "INEXISTANT", null, 0, 20, null, null);
            assertThatThrownBy(() -> sut.lister(listLotsQuery))
                    .isInstanceOf(SenPnaException.class);
        }

        @Test
        @DisplayName("statut absent → aucun filtre, aucune exception")
        void statutAbsent_aucunFiltre() {
            when(entrepotScopeGuard.entrepotIdPourLecture(any())).thenReturn(null);
            when(lotRepositoryPort.search(any(), any())).thenReturn(PageResult.of(List.of(), 0, 20, 0));

            LotPage page = sut.lister(new ListLotsQuery(null, null, null, null, null, 0, 20, null, null));

            assertThat(page.content()).isEmpty();
        }

        @Test
        @DisplayName("résout la portée de lecture (entrepôt effectif) via le guard")
        void resoutPorteeDeLecture() {
            UUID entrepotDemande = UUID.randomUUID();
            UUID entrepotEffectif = UUID.randomUUID();
            when(entrepotScopeGuard.entrepotIdPourLecture(entrepotDemande)).thenReturn(entrepotEffectif);
            when(lotRepositoryPort.search(any(), any())).thenReturn(PageResult.of(List.of(), 0, 20, 0));

            sut.lister(new ListLotsQuery(null, null, null, null, entrepotDemande, 0, 20, null, null));

            verify(entrepotScopeGuard).entrepotIdPourLecture(entrepotDemande);
        }
    }

    @Nested
    @DisplayName("ListerAlertesPeremptionUseCaseImpl")
    class ListerAlertesPeremption {

        ListerAlertesPeremptionUseCaseImpl sut;

        @BeforeEach
        void setUp() {
            sut = new ListerAlertesPeremptionUseCaseImpl(lotRepositoryPort, lotDetailAssembler, entrepotScopeGuard);
        }

        @Test
        @DisplayName("horizon négatif ou nul → IllegalArgumentException")
        void horizonInvalide_leveException() {
            var alertePeremptionQuery = new AlertePeremptionQuery(0, null, null, 0, 20);
            assertThatThrownBy(() -> sut.lister(alertePeremptionQuery))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("horizon valide → délègue à findExpirantAvant()")
        void horizonValide_delegue() {
            when(entrepotScopeGuard.entrepotIdPourLecture(any())).thenReturn(null);
            when(lotRepositoryPort.findExpirantAvant(any(), any())).thenReturn(PageResult.of(List.of(), 0, 20, 0));

            sut.lister(new AlertePeremptionQuery(30, null, null, 0, 20));

            verify(lotRepositoryPort).findExpirantAvant(any(), any());
        }
    }

    @Nested
    @DisplayName("MarquerLotsExpiresUseCaseImpl")
    class MarquerLotsExpires {

        MarquerLotsExpiresUseCaseImpl sut;

        @BeforeEach
        void setUp() {
            sut = new MarquerLotsExpiresUseCaseImpl(lotRepositoryPort);
        }

        @Test
        @DisplayName("aucun lot expiré → renvoie 0, aucune sauvegarde")
        void aucunLotExpire_renvoieZero() {
            when(lotRepositoryPort.findActifsExpires()).thenReturn(List.of());

            assertThat(sut.marquerExpires()).isZero();

            verify(lotRepositoryPort, never()).save(any());
        }

        @Test
        @DisplayName("lots expirés trouvés → chacun marqué EXPIRE et sauvegardé")
        void lotsExpiresTrouves_chacunMarqueEtSauvegarde() {
            Lot lot1 = Lot.creer(new Lot.CreationCommand("L1", MedicamentId.generate(), FournisseurId.generate(),
                    LocalDate.now().minusMonths(6), LocalDate.now().minusDays(1), BigDecimal.TEN, BigDecimal.TEN));
            Lot lot2 = Lot.creer(new Lot.CreationCommand("L2", MedicamentId.generate(), FournisseurId.generate(),
                    LocalDate.now().minusMonths(6), LocalDate.now().minusDays(1), BigDecimal.TEN, BigDecimal.TEN));
            when(lotRepositoryPort.findActifsExpires()).thenReturn(List.of(lot1, lot2));
            when(lotRepositoryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));

            int result = sut.marquerExpires();

            assertThat(result).isEqualTo(2);
            verify(lotRepositoryPort, times(2)).save(any());
        }
    }
}
