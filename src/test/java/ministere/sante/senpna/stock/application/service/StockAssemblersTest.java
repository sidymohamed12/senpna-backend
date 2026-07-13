package ministere.sante.senpna.stock.application.service;

import ministere.sante.senpna.fournisseur.domain.valueobject.FournisseurId;
import ministere.sante.senpna.medicament.domain.valueobject.MedicamentId;
import ministere.sante.senpna.organisation.domain.valueobject.EntrepotId;
import ministere.sante.senpna.stock.domain.command.LotCommands.LotDetail;
import ministere.sante.senpna.stock.domain.command.MouvementStockCommands.MouvementDetail;
import ministere.sante.senpna.stock.domain.command.StockCommands.StockDetail;
import ministere.sante.senpna.stock.domain.model.Lot;
import ministere.sante.senpna.stock.domain.model.MouvementStock;
import ministere.sante.senpna.stock.domain.model.Stock;
import ministere.sante.senpna.stock.domain.valueobject.LotId;
import ministere.sante.senpna.stock.domain.valueobject.SensMouvement;
import ministere.sante.senpna.stock.domain.valueobject.TypeMouvement;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Assembleurs de détail du module stock")
class StockAssemblersTest {

    @Test
    @DisplayName("LotDetailAssembler reporte fidèlement chaque champ, y compris les indicateurs dérivés")
    void lotDetailAssembler_reporteChaqueChamp() {
        Lot lot = Lot.creer("LOT-001", MedicamentId.generate(), FournisseurId.generate(),
                LocalDate.now().minusMonths(1), LocalDate.now().plusMonths(6), new BigDecimal("10.00"),
                new BigDecimal("15.00"));

        LotDetail detail = new LotDetailAssembler().assembler(lot);

        assertThat(detail.numeroLot()).isEqualTo("LOT-001");
        assertThat(detail.statut()).isEqualTo("ACTIF");
        assertThat(detail.expire()).isFalse();
        assertThat(detail.prixVente()).isEqualByComparingTo("15.00");
    }

    @Test
    @DisplayName("StockDetailAssembler reporte fidèlement chaque champ, y compris les indicateurs de rupture")
    void stockDetailAssembler_reporteChaqueChamp() {
        Stock stock = Stock.ouvrir(EntrepotId.generate(), LotId.generate(), MedicamentId.generate(),
                new BigDecimal("10"));
        stock.entrer(new BigDecimal("100"));
        stock.reserver(new BigDecimal("20"));

        StockDetail detail = new StockDetailAssembler().assembler(stock);

        assertThat(detail.quantiteDisponible()).isEqualByComparingTo("100");
        assertThat(detail.quantiteReservee()).isEqualByComparingTo("20");
        assertThat(detail.quantiteDisponibleALaVente()).isEqualByComparingTo("80");
        assertThat(detail.enRupture()).isFalse();
    }

    @Test
    @DisplayName("MouvementDetailAssembler gère un entrepôt source/destination null")
    void mouvementDetailAssembler_gereEntrepotNull() {
        MouvementStock mouvement = MouvementStock.creer(TypeMouvement.ENTREE_ACHAT, SensMouvement.ENTREE, null,
                EntrepotId.generate(), null, LotId.generate(), MedicamentId.generate(), new BigDecimal("50"),
                "REF-1", "Motif", UUID.randomUUID());

        MouvementDetail detail = new MouvementDetailAssembler().assembler(mouvement);

        assertThat(detail.entrepotSourceId()).isNull();
        assertThat(detail.entrepotDestinationId()).isNotNull();
        assertThat(detail.typeMouvement()).isEqualTo("ENTREE_ACHAT");
        assertThat(detail.quantite()).isEqualByComparingTo("50");
    }
}
