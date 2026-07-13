package ministere.sante.senpna.stock.infrastructure.persistence.mapper;

import ministere.sante.senpna.stock.domain.model.Lot;
import ministere.sante.senpna.stock.domain.model.MouvementStock;
import ministere.sante.senpna.stock.domain.model.Stock;
import ministere.sante.senpna.stock.domain.valueobject.LotId;
import ministere.sante.senpna.stock.domain.valueobject.SensMouvement;
import ministere.sante.senpna.stock.domain.valueobject.TypeMouvement;
import ministere.sante.senpna.stock.infrastructure.persistence.entity.LotJpaEntity;
import ministere.sante.senpna.stock.infrastructure.persistence.entity.MouvementStockJpaEntity;
import ministere.sante.senpna.stock.infrastructure.persistence.entity.StockJpaEntity;
import ministere.sante.senpna.fournisseur.domain.valueobject.FournisseurId;
import ministere.sante.senpna.medicament.domain.valueobject.MedicamentId;
import ministere.sante.senpna.organisation.domain.valueobject.EntrepotId;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Mappers de persistence du module stock")
class StockMappersTest {

    LotMapper lotMapper = new LotMapper();
    StockMapper stockMapper = new StockMapper();
    MouvementStockMapper mouvementStockMapper = new MouvementStockMapper();

    @Test
    @DisplayName("LotMapper : aller-retour préserve l'état")
    void lotMapper_allerRetourPreserveEtat() {
        Lot original = Lot.creer("LOT-001", MedicamentId.generate(), FournisseurId.generate(),
                LocalDate.now().minusMonths(1), LocalDate.now().plusMonths(6), new BigDecimal("10.00"),
                new BigDecimal("15.00"));

        LotJpaEntity entity = lotMapper.toEntity(original);
        Lot restaure = lotMapper.toDomain(entity);

        assertThat(restaure.getId()).isEqualTo(original.getId());
        assertThat(restaure.getNumeroLot()).isEqualTo("LOT-001");
        assertThat(restaure.getStatut()).isEqualTo(original.getStatut());
    }

    @Test
    @DisplayName("StockMapper : aller-retour préserve l'état, y compris les quantités")
    void stockMapper_allerRetourPreserveEtat() {
        Stock original = Stock.ouvrir(EntrepotId.generate(), LotId.generate(), MedicamentId.generate(),
                new BigDecimal("10"));
        original.entrer(new BigDecimal("100"));
        original.reserver(new BigDecimal("20"));

        StockJpaEntity entity = stockMapper.toEntity(original);
        Stock restaure = stockMapper.toDomain(entity);

        assertThat(restaure.getId()).isEqualTo(original.getId());
        assertThat(restaure.getQuantiteDisponible()).isEqualByComparingTo("100");
        assertThat(restaure.getQuantiteReservee()).isEqualByComparingTo("20");
    }

    @Test
    @DisplayName("MouvementStockMapper : aller-retour préserve l'état, entrepôt source null géré")
    void mouvementStockMapper_allerRetourAvecEntrepotSourceNull() {
        MouvementStock original = MouvementStock.creer(TypeMouvement.ENTREE_ACHAT, SensMouvement.ENTREE, null,
                EntrepotId.generate(), null, LotId.generate(), MedicamentId.generate(), new BigDecimal("50"),
                "REF-1", "Motif", UUID.randomUUID());

        MouvementStockJpaEntity entity = mouvementStockMapper.toEntity(original);
        MouvementStock restaure = mouvementStockMapper.toDomain(entity);

        assertThat(restaure.getId()).isEqualTo(original.getId());
        assertThat(restaure.getEntrepotSourceId()).isNull();
        assertThat(restaure.getEntrepotDestinationId()).isNotNull();
        assertThat(restaure.getQuantite()).isEqualByComparingTo("50");
    }
}
