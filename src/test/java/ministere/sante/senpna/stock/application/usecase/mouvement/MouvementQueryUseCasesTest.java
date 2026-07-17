package ministere.sante.senpna.stock.application.usecase.mouvement;

import ministere.sante.senpna.shared.domain.exception.SenPnaException;
import ministere.sante.senpna.shared.domain.valueobject.PageResult;
import ministere.sante.senpna.stock.application.service.EntrepotScopeGuard;
import ministere.sante.senpna.stock.application.service.MouvementDetailAssembler;
import ministere.sante.senpna.stock.domain.command.MouvementStockCommands.GetMouvementQuery;
import ministere.sante.senpna.stock.domain.command.MouvementStockCommands.ListMouvementsQuery;
import ministere.sante.senpna.stock.domain.command.MouvementStockCommands.MouvementDetail;
import ministere.sante.senpna.stock.domain.command.MouvementStockCommands.MouvementPage;
import ministere.sante.senpna.stock.domain.exception.PorteeEntrepotInterditeException;
import ministere.sante.senpna.stock.domain.exception.mouvement.MouvementStockIntrouvableException;
import ministere.sante.senpna.stock.domain.model.MouvementStock;
import ministere.sante.senpna.stock.domain.port.out.MouvementStockRepositoryPort;
import ministere.sante.senpna.stock.domain.valueobject.LotId;
import ministere.sante.senpna.stock.domain.valueobject.MouvementStockId;
import ministere.sante.senpna.stock.domain.valueobject.SensMouvement;
import ministere.sante.senpna.stock.domain.valueobject.TypeMouvement;
import ministere.sante.senpna.medicament.domain.valueobject.MedicamentId;
import ministere.sante.senpna.organisation.domain.valueobject.EntrepotId;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetMouvementUseCaseImpl / ListMouvementsUseCaseImpl")
class MouvementQueryUseCasesTest {

    @Mock
    MouvementStockRepositoryPort mouvementStockRepositoryPort;
    @Mock
    MouvementDetailAssembler mouvementDetailAssembler;
    @Mock
    EntrepotScopeGuard entrepotScopeGuard;

    private MouvementStock mouvement(EntrepotId source, EntrepotId destination) {
        return MouvementStock.creer(new MouvementStock.CreationCommand(TypeMouvement.SORTIE_TRANSFERT, SensMouvement.SORTIE, source, destination, null,
                LotId.generate(), MedicamentId.generate(), new BigDecimal("10"), "REF", "Motif",
                UUID.randomUUID()));
    }

    @Nested
    @DisplayName("GetMouvementUseCaseImpl")
    class Get {

        GetMouvementUseCaseImpl sut;

        @BeforeEach
        void setUp() {
            sut = new GetMouvementUseCaseImpl(mouvementStockRepositoryPort, mouvementDetailAssembler,
                    entrepotScopeGuard);
        }

        @Test
        @DisplayName("introuvable → MouvementStockIntrouvableException")
        void introuvable_leveException() {
            when(mouvementStockRepositoryPort.findById(any())).thenReturn(Optional.empty());

            var getMouvementQuery = new GetMouvementQuery(UUID.randomUUID());
            assertThatThrownBy(() -> sut.obtenir(getMouvementQuery))
                    .isInstanceOf(MouvementStockIntrouvableException.class);
        }

        @Test
        @DisplayName("acteur national → toujours autorisé, quel que soit l'entrepôt")
        void acteurNational_toujoursAutorise() {
            MouvementStock m = mouvement(EntrepotId.generate(), EntrepotId.generate());
            MouvementDetail detailAttendu = mock(MouvementDetail.class);
            when(mouvementStockRepositoryPort.findById(MouvementStockId.of(m.getId().getValue())))
                    .thenReturn(Optional.of(m));
            when(entrepotScopeGuard.estActeurNational()).thenReturn(true);
            when(mouvementDetailAssembler.assembler(m)).thenReturn(detailAttendu);

            var resultat = sut.obtenir(new GetMouvementQuery(m.getId().getValue()));

            assertThat(resultat).isSameAs(detailAttendu);
        }

        @Test
        @DisplayName("acteur PRA impliqué (source ou destination) → autorisé")
        void acteurPraImplique_autorise() {
            EntrepotId monEntrepot = EntrepotId.generate();
            MouvementStock m = mouvement(EntrepotId.generate(), monEntrepot);
            MouvementDetail detailAttendu = mock(MouvementDetail.class);
            when(mouvementStockRepositoryPort.findById(MouvementStockId.of(m.getId().getValue())))
                    .thenReturn(Optional.of(m));
            when(entrepotScopeGuard.estActeurNational()).thenReturn(false);
            when(entrepotScopeGuard.entrepotIdCourant()).thenReturn(monEntrepot.getValue());
            when(mouvementDetailAssembler.assembler(m)).thenReturn(detailAttendu);

            var resultat = sut.obtenir(new GetMouvementQuery(m.getId().getValue()));

            assertThat(resultat).isSameAs(detailAttendu);
        }

        @Test
        @DisplayName("acteur PRA non impliqué → PorteeEntrepotInterditeException")
        void acteurPraNonImplique_leveException() {
            MouvementStock m = mouvement(EntrepotId.generate(), EntrepotId.generate());
            when(mouvementStockRepositoryPort.findById(MouvementStockId.of(m.getId().getValue())))
                    .thenReturn(Optional.of(m));
            when(entrepotScopeGuard.estActeurNational()).thenReturn(false);
            when(entrepotScopeGuard.entrepotIdCourant()).thenReturn(UUID.randomUUID());

            var getMouvementQuery = new GetMouvementQuery(m.getId().getValue());
            assertThatThrownBy(() -> sut.obtenir(getMouvementQuery))
                    .isInstanceOf(PorteeEntrepotInterditeException.class);
        }
    }

    @Nested
    @DisplayName("ListMouvementsUseCaseImpl")
    class ListMouvements {

        ListMouvementsUseCaseImpl sut;

        @BeforeEach
        void setUp() {
            sut = new ListMouvementsUseCaseImpl(mouvementStockRepositoryPort, mouvementDetailAssembler,
                    entrepotScopeGuard);
        }

        @Test
        @DisplayName("type de mouvement invalide → ValidationException")
        void typeInvalide_leveException() {
            when(entrepotScopeGuard.entrepotIdPourLecture(any())).thenReturn(null);

            var listMouvementsQuery = new ListMouvementsQuery(null, null, null, "INEXISTANT", null, null, null, null, 0,
                    20, null, null);
            assertThatThrownBy(() -> sut.lister(listMouvementsQuery)).isInstanceOf(SenPnaException.class);
        }

        @Test
        @DisplayName("sens invalide → ValidationException")
        void sensInvalide_leveException() {
            when(entrepotScopeGuard.entrepotIdPourLecture(any())).thenReturn(null);

            var listMouvementsQuery = new ListMouvementsQuery(null, null, null, null, "INEXISTANT",
                    null, null, null, 0, 20, null, null);
            assertThatThrownBy(() -> sut.lister(listMouvementsQuery)).isInstanceOf(SenPnaException.class);
        }

        @Test
        @DisplayName("query valide → délègue à search() et mappe les résultats")
        void queryValide_delegueEtMappe() {
            when(entrepotScopeGuard.entrepotIdPourLecture(any())).thenReturn(null);
            when(mouvementStockRepositoryPort.search(any(), any())).thenReturn(PageResult.of(List.of(), 0, 20, 0));

            MouvementPage page = sut.lister(
                    new ListMouvementsQuery(null, null, null, null, null, null, null, null, 0, 20, null, null));

            assertThat(page.content()).isEmpty();
        }
    }
}
