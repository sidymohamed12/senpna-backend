package ministere.sante.senpna.stock.application.usecase.mouvement;

import ministere.sante.senpna.organisation.domain.exception.EntrepotIntrouvableException;
import ministere.sante.senpna.organisation.domain.model.Entrepot;
import ministere.sante.senpna.organisation.domain.port.out.EntrepotRepositoryPort;
import ministere.sante.senpna.organisation.domain.valueobject.RegionId;
import ministere.sante.senpna.shared.domain.exception.ErrorCategory;
import ministere.sante.senpna.shared.domain.exception.SenPnaException;
import ministere.sante.senpna.stock.application.service.EntrepotScopeGuard;
import ministere.sante.senpna.stock.application.service.StockDetailAssembler;
import ministere.sante.senpna.stock.domain.command.StockCommands.EntreeStockCommand;
import ministere.sante.senpna.stock.domain.exception.lot.LotIntrouvableException;
import ministere.sante.senpna.stock.domain.model.Lot;
import ministere.sante.senpna.stock.domain.model.Stock;
import ministere.sante.senpna.stock.domain.port.out.LotRepositoryPort;
import ministere.sante.senpna.stock.domain.port.out.MouvementStockRepositoryPort;
import ministere.sante.senpna.stock.domain.port.out.StockRepositoryPort;
import ministere.sante.senpna.fournisseur.domain.valueobject.FournisseurId;
import ministere.sante.senpna.medicament.domain.valueobject.MedicamentId;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("EntreeStockUseCaseImpl — entrée en stock")
class EntreeStockUseCaseImplTest {

    @Mock
    StockRepositoryPort stockRepositoryPort;
    @Mock
    LotRepositoryPort lotRepositoryPort;
    @Mock
    EntrepotRepositoryPort entrepotRepositoryPort;
    @Mock
    MouvementStockRepositoryPort mouvementStockRepositoryPort;
    @Mock
    StockDetailAssembler stockDetailAssembler;
    @Mock
    EntrepotScopeGuard entrepotScopeGuard;

    EntreeStockUseCaseImpl sut;

    UUID entrepotUuid;
    UUID lotUuid;
    Lot lot;

    @BeforeEach
    void setUp() {
        sut = new EntreeStockUseCaseImpl(stockRepositoryPort, lotRepositoryPort, entrepotRepositoryPort,
                mouvementStockRepositoryPort, stockDetailAssembler, entrepotScopeGuard);
        entrepotUuid = UUID.randomUUID();
        lotUuid = UUID.randomUUID();
        lot = Lot.creer(new Lot.CreationCommand("LOT-1", MedicamentId.generate(), FournisseurId.generate(), LocalDate.now().minusMonths(1),
                LocalDate.now().plusMonths(6), BigDecimal.TEN, BigDecimal.TEN));
    }

    private EntreeStockCommand commande(String type) {
        return new EntreeStockCommand(entrepotUuid, lotUuid, new BigDecimal("50"), type, null, "REF", "Motif",
                UUID.randomUUID());
    }

    @Test
    @DisplayName("lot introuvable → LotIntrouvableException")
    void lotIntrouvable_leveException() {
        when(lotRepositoryPort.findById(any())).thenReturn(Optional.empty());

        var command = commande("ENTREE_ACHAT");
        assertThatThrownBy(() -> sut.entrer(command)).isInstanceOf(LotIntrouvableException.class);
    }

    @Test
    @DisplayName("entrepôt introuvable → EntrepotIntrouvableException")
    void entrepotIntrouvable_leveException() {
        when(lotRepositoryPort.findById(any())).thenReturn(Optional.of(lot));
        when(entrepotRepositoryPort.findById(any())).thenReturn(Optional.empty());

        var command = commande("ENTREE_ACHAT");
        assertThatThrownBy(() -> sut.entrer(command)).isInstanceOf(EntrepotIntrouvableException.class);
    }

    @Test
    @DisplayName("aucun stock existant pour (entrepôt, lot) → ouvre une nouvelle ligne à zéro")
    void aucunStockExistant_ouvreNouvelleLigne() {
        Entrepot entrepot = Entrepot.creerPra(new Entrepot.CreationCommand("PRA-1", "PRA 1", RegionId.generate(), "Adresse", "771111111"));
        when(lotRepositoryPort.findById(any())).thenReturn(Optional.of(lot));
        when(entrepotRepositoryPort.findById(any())).thenReturn(Optional.of(entrepot));
        when(stockRepositoryPort.findByEntrepotIdAndLotIdForUpdate(any(), any())).thenReturn(Optional.empty());
        when(stockRepositoryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));

        sut.entrer(commande("ENTREE_ACHAT"));

        org.mockito.ArgumentCaptor<Stock> captor = org.mockito.ArgumentCaptor.forClass(Stock.class);
        verify(stockRepositoryPort).save(captor.capture());
        assertThat(captor.getValue().getQuantiteDisponible()).isEqualByComparingTo("50");
    }

    @Test
    @DisplayName("stock existant → augmente la quantité disponible et enregistre le mouvement")
    void stockExistant_augmenteEtEnregistreMovement() {
        Entrepot entrepot = Entrepot.creerPra(new Entrepot.CreationCommand("PRA-1", "PRA 1", RegionId.generate(), "Adresse", "771111111"));
        Stock stockExistant = Stock.ouvrir(new Stock.OuvertureCommand(entrepot.getId(), lot.getId(), lot.getMedicamentId(), null));
        stockExistant.entrer(new BigDecimal("10"));
        when(lotRepositoryPort.findById(any())).thenReturn(Optional.of(lot));
        when(entrepotRepositoryPort.findById(any())).thenReturn(Optional.of(entrepot));
        when(stockRepositoryPort.findByEntrepotIdAndLotIdForUpdate(any(), any()))
                .thenReturn(Optional.of(stockExistant));
        when(stockRepositoryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));

        sut.entrer(commande("ENTREE_ACHAT"));

        assertThat(stockExistant.getQuantiteDisponible()).isEqualByComparingTo("60");
        verify(mouvementStockRepositoryPort).save(any());
    }

    @Test
    @DisplayName("type de mouvement invalide → SenPnaException (catégorie VALIDATION)")
    void typeMouvementInvalide_leveException() {
        Entrepot entrepot = Entrepot.creerPra(new Entrepot.CreationCommand("PRA-1", "PRA 1", RegionId.generate(), "Adresse", "771111111"));
        when(lotRepositoryPort.findById(any())).thenReturn(Optional.of(lot));
        when(entrepotRepositoryPort.findById(any())).thenReturn(Optional.of(entrepot));

        var command = commande("TYPE_INEXISTANT");
        assertThatThrownBy(() -> sut.entrer(command))
                .isInstanceOf(ministere.sante.senpna.shared.domain.exception.SenPnaException.class)
                .satisfies(ex -> assertThat(
                        ((SenPnaException) ex).getCategory())
                        .isEqualTo(ErrorCategory.VALIDATION));
    }
}
