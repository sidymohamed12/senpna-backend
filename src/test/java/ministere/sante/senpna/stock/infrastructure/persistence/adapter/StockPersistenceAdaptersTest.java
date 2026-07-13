package ministere.sante.senpna.stock.infrastructure.persistence.adapter;

import ministere.sante.senpna.stock.domain.criteria.LotSearchCriteria;
import ministere.sante.senpna.stock.domain.criteria.MouvementSearchCriteria;
import ministere.sante.senpna.stock.domain.criteria.StockSearchCriteria;
import ministere.sante.senpna.stock.domain.model.Lot;
import ministere.sante.senpna.stock.domain.model.MouvementStock;
import ministere.sante.senpna.stock.domain.model.Stock;
import ministere.sante.senpna.stock.domain.valueobject.LotId;
import ministere.sante.senpna.stock.domain.valueobject.SensMouvement;
import ministere.sante.senpna.stock.domain.valueobject.StatutLot;
import ministere.sante.senpna.stock.domain.valueobject.TypeMouvement;
import ministere.sante.senpna.stock.infrastructure.persistence.mapper.LotMapper;
import ministere.sante.senpna.stock.infrastructure.persistence.mapper.MouvementStockMapper;
import ministere.sante.senpna.stock.infrastructure.persistence.mapper.StockMapper;
import ministere.sante.senpna.shared.domain.valueobject.PageRequest;
import ministere.sante.senpna.shared.domain.valueobject.PageResult;
import ministere.sante.senpna.fournisseur.domain.valueobject.FournisseurId;
import ministere.sante.senpna.medicament.domain.valueobject.MedicamentId;
import ministere.sante.senpna.organisation.domain.valueobject.EntrepotId;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Tag("integration")
@Import({ LotRepositoryAdapter.class, LotMapper.class, StockRepositoryAdapter.class, StockMapper.class,
                MouvementStockRepositoryAdapter.class, MouvementStockMapper.class })
@DisplayName("Adaptateurs de persistence stock — H2")
class StockPersistenceAdaptersTest {

        @Autowired
        LotRepositoryAdapter lotAdapter;
        @Autowired
        StockRepositoryAdapter stockAdapter;
        @Autowired
        MouvementStockRepositoryAdapter mouvementAdapter;
        @Autowired
        TestEntityManager entityManager;

        MedicamentId medicamentA;
        FournisseurId fournisseurX;
        EntrepotId entrepot1;

        @BeforeEach
        void setUp() {
                medicamentA = MedicamentId.generate();
                fournisseurX = FournisseurId.generate();
                entrepot1 = EntrepotId.generate();
        }

        @Nested
        @DisplayName("LotRepositoryAdapter")
        class LotTests {

                @Test
                @DisplayName("existsByMedicamentIdAndNumeroLotIgnoreCase() insensible à la casse")
                void existsByMedicamentIdEtNumero_insensibleCasse() {
                        Lot lot = Lot.creer("LOT-ABC", medicamentA, fournisseurX, LocalDate.now().minusMonths(1),
                                        LocalDate.now().plusMonths(6), BigDecimal.TEN, BigDecimal.TEN);
                        lotAdapter.save(lot);
                        entityManager.flush();
                        entityManager.clear();

                        assertThat(lotAdapter.existsByMedicamentIdAndNumeroLotIgnoreCase(medicamentA, "lot-abc"))
                                        .isTrue();
                }

                @Test
                @DisplayName("search() filtre par statut et par médicament")
                void search_filtreParStatutEtMedicament() {
                        Lot actif = Lot.creer("L1", medicamentA, fournisseurX, LocalDate.now().minusMonths(1),
                                        LocalDate.now().plusMonths(6), BigDecimal.TEN, BigDecimal.TEN);
                        Lot bloque = Lot.creer("L2", medicamentA, fournisseurX, LocalDate.now().minusMonths(1),
                                        LocalDate.now().plusMonths(6), BigDecimal.TEN, BigDecimal.TEN);
                        bloque.bloquer();
                        lotAdapter.save(actif);
                        lotAdapter.save(bloque);
                        entityManager.flush();
                        entityManager.clear();

                        PageResult<Lot> result = lotAdapter.search(
                                        new LotSearchCriteria(null, medicamentA.getValue(), null, StatutLot.ACTIF,
                                                        null),
                                        PageRequest.of(0, 20, null, null));

                        assertThat(result.content()).extracting(Lot::getNumeroLot).containsExactly("L1");
                }

                @Test
                @DisplayName("findActifsExpires() ne renvoie que les lots ACTIF expirés")
                void findActifsExpires() {
                        Lot expire = Lot.creer("EXP", medicamentA, fournisseurX, LocalDate.now().minusMonths(6),
                                        LocalDate.now().minusDays(1), BigDecimal.TEN, BigDecimal.TEN);
                        Lot valide = Lot.creer("OK", medicamentA, fournisseurX, LocalDate.now().minusMonths(1),
                                        LocalDate.now().plusMonths(6), BigDecimal.TEN, BigDecimal.TEN);
                        lotAdapter.save(expire);
                        lotAdapter.save(valide);
                        entityManager.flush();
                        entityManager.clear();

                        List<Lot> result = lotAdapter.findActifsExpires();

                        assertThat(result).extracting(Lot::getNumeroLot).containsExactly("EXP");
                }

                @Test
                @DisplayName("findActifsNonExpiresParMedicamentTriesFefo() trie par date d'expiration croissante")
                void findActifsNonExpires_trieFefo() {
                        Lot expirationLointaine = Lot.creer("L-LOIN", medicamentA, fournisseurX,
                                        LocalDate.now().minusMonths(1),
                                        LocalDate.now().plusMonths(12), BigDecimal.TEN, BigDecimal.TEN);
                        Lot expirationProche = Lot.creer("L-PROCHE", medicamentA, fournisseurX,
                                        LocalDate.now().minusMonths(1),
                                        LocalDate.now().plusMonths(2), BigDecimal.TEN, BigDecimal.TEN);
                        lotAdapter.save(expirationLointaine);
                        lotAdapter.save(expirationProche);
                        entityManager.flush();
                        entityManager.clear();

                        List<Lot> result = lotAdapter.findActifsNonExpiresParMedicamentTriesFefo(medicamentA);

                        assertThat(result).extracting(Lot::getNumeroLot).containsExactly("L-PROCHE", "L-LOIN");
                }
        }

        @Nested
        @DisplayName("StockRepositoryAdapter")
        class StockTests {

                @Test
                @DisplayName("findByEntrepotIdAndLotId() retrouve la ligne de stock")
                void findByEntrepotIdAndLotId() {
                        LotId lotId = LotId.generate();
                        Stock stock = Stock.ouvrir(entrepot1, lotId, medicamentA, null);
                        stock.entrer(BigDecimal.TEN);
                        stockAdapter.save(stock);
                        entityManager.flush();
                        entityManager.clear();

                        assertThat(stockAdapter.findByEntrepotIdAndLotId(entrepot1, lotId)).isPresent();
                }

                @Test
                @DisplayName("search() filtre par rupture")
                void search_filtreParRupture() {
                        Stock enRupture = Stock.ouvrir(entrepot1, LotId.generate(), medicamentA, null);
                        Stock disponible = Stock.ouvrir(entrepot1, LotId.generate(), medicamentA, null);
                        disponible.entrer(new BigDecimal("50"));
                        stockAdapter.save(enRupture);
                        stockAdapter.save(disponible);
                        entityManager.flush();
                        entityManager.clear();

                        PageResult<Stock> result = stockAdapter.search(
                                        new StockSearchCriteria(entrepot1.getValue(), null, null, true, null),
                                        PageRequest.of(0, 20, null, null));

                        assertThat(result.content()).hasSize(1);
                        assertThat(result.content().get(0).getQuantiteDisponible()).isZero();
                }
        }

        @Nested
        @DisplayName("MouvementStockRepositoryAdapter")
        class MouvementTests {

                @Test
                @DisplayName("search() filtre par entrepôt (source OU destination)")
                void search_filtreParEntrepotSourceOuDestination() {
                        EntrepotId autreEntrepot = EntrepotId.generate();
                        MouvementStock enSource = MouvementStock.creer(TypeMouvement.SORTIE_TRANSFERT,
                                        SensMouvement.SORTIE,
                                        entrepot1, autreEntrepot, null, LotId.generate(), medicamentA, BigDecimal.TEN,
                                        "REF1", "M1",
                                        UUID.randomUUID());
                        MouvementStock enDestination = MouvementStock.creer(TypeMouvement.SORTIE_TRANSFERT,
                                        SensMouvement.SORTIE,
                                        autreEntrepot, entrepot1, null, LotId.generate(), medicamentA, BigDecimal.TEN,
                                        "REF2", "M2",
                                        UUID.randomUUID());
                        MouvementStock sansLien = MouvementStock.creer(TypeMouvement.ENTREE_ACHAT, SensMouvement.ENTREE,
                                        null,
                                        autreEntrepot, null, LotId.generate(), medicamentA, BigDecimal.TEN, "REF3",
                                        "M3",
                                        UUID.randomUUID());
                        mouvementAdapter.save(enSource);
                        mouvementAdapter.save(enDestination);
                        mouvementAdapter.save(sansLien);
                        entityManager.flush();
                        entityManager.clear();

                        PageResult<MouvementStock> result = mouvementAdapter.search(
                                        new MouvementSearchCriteria(null, null, entrepot1.getValue(), null, null, null,
                                                        null, null),
                                        PageRequest.of(0, 20, null, null));

                        assertThat(result.content()).extracting(MouvementStock::getReferenceDocument)
                                        .containsExactlyInAnyOrder("REF1", "REF2");
                }
        }
}
