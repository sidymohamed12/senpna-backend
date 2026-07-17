package ministere.sante.senpna.stock.infrastructure.persistence.specification;

import ministere.sante.senpna.stock.infrastructure.persistence.entity.StockJpaEntity;
import ministere.sante.senpna.stock.infrastructure.persistence.repository.StockJpaRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("StockSpecifications — filtres JPA Specification sur les stocks")
class StockSpecificationsTest {

    @Autowired
    StockJpaRepository stockJpaRepository;

    UUID entrepotA = UUID.randomUUID();
    UUID entrepotB = UUID.randomUUID();
    UUID lotA = UUID.randomUUID();
    UUID lotB = UUID.randomUUID();
    UUID medicamentA = UUID.randomUUID();
    UUID medicamentB = UUID.randomUUID();

    StockJpaEntity stockDisponible;
    StockJpaEntity stockEnRuptureAvecSeuil;

    @BeforeEach
    void setUp() {
        // Disponible: 100 - 10 = 90 > 0, pas de seuil défini.
        stockDisponible = StockJpaEntity.builder()
            .id(UUID.randomUUID())
            .entrepotId(entrepotA)
            .lotId(lotA)
            .medicamentId(medicamentA)
            .quantiteDisponible(new BigDecimal("100.0000"))
            .quantiteReservee(new BigDecimal("10.0000"))
            .quantiteEnCommande(BigDecimal.ZERO)
            .seuilAlerte(null)
            .build();
        // Rupture: 20 - 20 = 0 <= 0, et seuil (30) >= 0 disponible à la vente.
        stockEnRuptureAvecSeuil = StockJpaEntity.builder()
            .id(UUID.randomUUID())
            .entrepotId(entrepotB)
            .lotId(lotB)
            .medicamentId(medicamentB)
            .quantiteDisponible(new BigDecimal("20.0000"))
            .quantiteReservee(new BigDecimal("20.0000"))
            .quantiteEnCommande(BigDecimal.ZERO)
            .seuilAlerte(new BigDecimal("30.0000"))
            .build();

        stockJpaRepository.saveAll(List.of(stockDisponible, stockEnRuptureAvecSeuil));
    }

    @Nested
    @DisplayName("entrepotId()")
    class EntrepotIdFiltre {

        @Test
        @DisplayName("null → specification null")
        void nul_specificationNull() {
            assertThat(StockSpecifications.entrepotId(null)).isNull();
        }

        @Test
        @DisplayName("filtre uniquement les stocks de l'entrepôt donné")
        void filtreParEntrepot() {
            List<StockJpaEntity> resultats = stockJpaRepository.findAll(StockSpecifications.entrepotId(entrepotA));

            assertThat(resultats).extracting(StockJpaEntity::getLotId).containsExactly(lotA);
        }
    }

    @Nested
    @DisplayName("lotId()")
    class LotIdFiltre {

        @Test
        @DisplayName("null → specification null")
        void nul_specificationNull() {
            assertThat(StockSpecifications.lotId(null)).isNull();
        }

        @Test
        @DisplayName("filtre uniquement les stocks du lot donné")
        void filtreParLot() {
            List<StockJpaEntity> resultats = stockJpaRepository.findAll(StockSpecifications.lotId(lotB));

            assertThat(resultats).extracting(StockJpaEntity::getLotId).containsExactly(lotB);
        }
    }

    @Nested
    @DisplayName("medicamentId()")
    class MedicamentIdFiltre {

        @Test
        @DisplayName("null → specification null")
        void nul_specificationNull() {
            assertThat(StockSpecifications.medicamentId(null)).isNull();
        }

        @Test
        @DisplayName("filtre uniquement les stocks du médicament donné")
        void filtreParMedicament() {
            List<StockJpaEntity> resultats = stockJpaRepository
                    .findAll(StockSpecifications.medicamentId(medicamentA));

            assertThat(resultats).extracting(StockJpaEntity::getMedicamentId).containsExactly(medicamentA);
        }
    }

    @Nested
    @DisplayName("enRupture()")
    class EnRuptureFiltre {

        @Test
        @DisplayName("false ou null → specification null")
        void fauxOuNul_specificationNull() {
            assertThat(StockSpecifications.enRupture(null)).isNull();
            assertThat(StockSpecifications.enRupture(false)).isNull();
        }

        @Test
        @DisplayName("true → ne retient que les stocks dont la quantité disponible à la vente est ≤ 0")
        void vrai_filtreLesRuptures() {
            List<StockJpaEntity> resultats = stockJpaRepository.findAll(StockSpecifications.enRupture(true));

            assertThat(resultats).extracting(StockJpaEntity::getLotId).containsExactly(lotB);
        }
    }

    @Nested
    @DisplayName("seuilAtteint()")
    class SeuilAtteintFiltre {

        @Test
        @DisplayName("false ou null → specification null")
        void fauxOuNul_specificationNull() {
            assertThat(StockSpecifications.seuilAtteint(null)).isNull();
            assertThat(StockSpecifications.seuilAtteint(false)).isNull();
        }

        @Test
        @DisplayName("true → ne retient que les stocks avec seuil défini et atteint")
        void vrai_filtreLeSeuilAtteint() {
            List<StockJpaEntity> resultats = stockJpaRepository.findAll(StockSpecifications.seuilAtteint(true));

            assertThat(resultats).extracting(StockJpaEntity::getLotId).containsExactly(lotB);
        }
    }

    @Nested
    @DisplayName("combiner()")
    class Combiner {

        @Test
        @DisplayName("tous les critères null → aucun filtre, tous les stocks renvoyés")
        void tousCriteresNull_aucunFiltre() {
            Specification<StockJpaEntity> spec = StockSpecifications.combiner(null, null, null, null, null);

            assertThat(stockJpaRepository.findAll(spec)).hasSize(2);
        }

        @Test
        @DisplayName("combinaison de tous les critères")
        void combinaisonComplete() {
            Specification<StockJpaEntity> spec = StockSpecifications.combiner(entrepotB, lotB, medicamentB, true,
                    true);

            List<StockJpaEntity> resultats = stockJpaRepository.findAll(spec);

            assertThat(resultats).extracting(StockJpaEntity::getLotId).containsExactly(lotB);
        }

        @Test
        @DisplayName("combinaison ne correspondant à aucun stock → résultat vide")
        void combinaisonSansCorrespondance() {
            Specification<StockJpaEntity> spec = StockSpecifications.combiner(entrepotA, null, null, true, null);

            assertThat(stockJpaRepository.findAll(spec)).isEmpty();
        }
    }
}
