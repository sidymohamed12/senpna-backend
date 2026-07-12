package ministere.sante.senpna.stock.infrastructure.persistence.specification;

import ministere.sante.senpna.stock.domain.valueobject.StatutLot;
import ministere.sante.senpna.stock.infrastructure.persistence.entity.LotJpaEntity;
import ministere.sante.senpna.stock.infrastructure.persistence.entity.StockJpaEntity;
import ministere.sante.senpna.stock.infrastructure.persistence.repository.LotJpaRepository;
import ministere.sante.senpna.stock.infrastructure.persistence.repository.StockJpaRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Tag("integration")
@DisplayName("LotSpecifications — filtres JPA Specification sur les lots")
class LotSpecificationsTest {

    @Autowired
    LotJpaRepository lotJpaRepository;

    @Autowired
    StockJpaRepository stockJpaRepository;

    @Autowired
    TestEntityManager entityManager;

    UUID medicamentA;
    UUID medicamentB;
    UUID fournisseurX;
    UUID fournisseurY;
    UUID entrepot1;
    UUID entrepot2;

    LotJpaEntity lotActifA;
    LotJpaEntity lotBloqueA;
    LotJpaEntity lotActifB;

    @BeforeEach
    void setUp() {
        medicamentA = UUID.randomUUID();
        medicamentB = UUID.randomUUID();
        fournisseurX = UUID.randomUUID();
        fournisseurY = UUID.randomUUID();
        entrepot1 = UUID.randomUUID();
        entrepot2 = UUID.randomUUID();

        lotActifA = creerLot("LOT-A-001", medicamentA, fournisseurX, LocalDate.now().plusMonths(6),
                StatutLot.ACTIF);
        lotBloqueA = creerLot("LOT-A-002", medicamentA, fournisseurY, LocalDate.now().plusDays(10),
                StatutLot.BLOQUE);
        lotActifB = creerLot("LOT-B-777", medicamentB, fournisseurX, LocalDate.now().plusYears(1),
                StatutLot.ACTIF);

        lotJpaRepository.saveAll(List.of(lotActifA, lotBloqueA, lotActifB));

        creerStock(entrepot1, lotActifA.getId(), medicamentA);
        // lotBloqueA et lotActifB n'ont volontairement aucune ligne de stock dans entrepot1

        entityManager.flush();
        entityManager.clear();
    }

    private LotJpaEntity creerLot(String numero, UUID medicamentId, UUID fournisseurId,
            LocalDate dateExpiration, StatutLot statut) {
        return new LotJpaEntity(
                UUID.randomUUID(), numero, medicamentId, fournisseurId,
                LocalDate.now().minusMonths(1), dateExpiration,
                new BigDecimal("10.00"), new BigDecimal("15.00"), statut.name());
    }

    private void creerStock(UUID entrepotId, UUID lotId, UUID medicamentId) {
        stockJpaRepository.save(new StockJpaEntity(
                UUID.randomUUID(), entrepotId, lotId, medicamentId,
                new BigDecimal("100.0000"), BigDecimal.ZERO, BigDecimal.ZERO, new BigDecimal("10.0000")));
    }

    @Nested
    @DisplayName("recherche()")
    class Recherche {

        @Test
        @DisplayName("texte null ou vide → specification null (aucun filtre)")
        void texteNulOuVide_specificationNull() {
            assertThat(LotSpecifications.recherche(null)).isNull();
            assertThat(LotSpecifications.recherche("  ")).isNull();
        }

        @Test
        @DisplayName("filtre insensible à la casse sur le numéro de lot")
        void filtreInsensibleCasse() {
            List<LotJpaEntity> resultats = lotJpaRepository.findAll(LotSpecifications.recherche("lot-a"));

            assertThat(resultats)
                    .extracting(LotJpaEntity::getNumeroLot)
                    .containsExactlyInAnyOrder("LOT-A-001", "LOT-A-002");
        }

        @Test
        @DisplayName("motif ne correspondant à aucun numéro de lot → résultat vide")
        void motifSansCorrespondance_resultatVide() {
            List<LotJpaEntity> resultats = lotJpaRepository.findAll(LotSpecifications.recherche("ZZZ-INEXISTANT"));

            assertThat(resultats).isEmpty();
        }
    }

    @Nested
    @DisplayName("medicamentId()")
    class MedicamentIdFiltre {

        @Test
        @DisplayName("null → specification null")
        void nul_specificationNull() {
            assertThat(LotSpecifications.medicamentId(null)).isNull();
        }

        @Test
        @DisplayName("filtre uniquement les lots du médicament donné")
        void filtreLotsDuMedicament() {
            List<LotJpaEntity> resultats = lotJpaRepository.findAll(LotSpecifications.medicamentId(medicamentB));

            assertThat(resultats).extracting(LotJpaEntity::getNumeroLot).containsExactly("LOT-B-777");
        }
    }

    @Nested
    @DisplayName("fournisseurId()")
    class FournisseurIdFiltre {

        @Test
        @DisplayName("null → specification null")
        void nul_specificationNull() {
            assertThat(LotSpecifications.fournisseurId(null)).isNull();
        }

        @Test
        @DisplayName("filtre uniquement les lots du fournisseur donné")
        void filtreLotsDuFournisseur() {
            List<LotJpaEntity> resultats = lotJpaRepository.findAll(LotSpecifications.fournisseurId(fournisseurX));

            assertThat(resultats)
                    .extracting(LotJpaEntity::getNumeroLot)
                    .containsExactlyInAnyOrder("LOT-A-001", "LOT-B-777");
        }
    }

    @Nested
    @DisplayName("statut()")
    class StatutFiltre {

        @Test
        @DisplayName("null → specification null")
        void nul_specificationNull() {
            assertThat(LotSpecifications.statut(null)).isNull();
        }

        @Test
        @DisplayName("filtre uniquement les lots du statut donné")
        void filtreLotsDuStatut() {
            List<LotJpaEntity> resultats = lotJpaRepository.findAll(LotSpecifications.statut(StatutLot.BLOQUE));

            assertThat(resultats).extracting(LotJpaEntity::getNumeroLot).containsExactly("LOT-A-002");
        }
    }

    @Nested
    @DisplayName("expirantAvant()")
    class ExpirantAvant {

        @Test
        @DisplayName("null → specification null")
        void nul_specificationNull() {
            assertThat(LotSpecifications.expirantAvant(null)).isNull();
        }

        @Test
        @DisplayName("ne retient que les lots ACTIFS expirant avant la date limite")
        void filtreLotsActifsExpirantAvant() {
            List<LotJpaEntity> resultats = lotJpaRepository.findAll(
                    LotSpecifications.expirantAvant(LocalDate.now().plusDays(30)));

            // lotBloqueA expire dans 10 jours mais est BLOQUE → exclu
            assertThat(resultats).isEmpty();
        }

        @Test
        @DisplayName("lot ACTIF expirant avant la date limite → inclus")
        void lotActifExpirantAvant_inclus() {
            List<LotJpaEntity> resultats = lotJpaRepository.findAll(
                    LotSpecifications.expirantAvant(LocalDate.now().plusMonths(7)));

            assertThat(resultats).extracting(LotJpaEntity::getNumeroLot).containsExactly("LOT-A-001");
        }
    }

    @Nested
    @DisplayName("possedeStockDansEntrepot()")
    class PossedeStockDansEntrepot {

        @Test
        @DisplayName("null → specification null")
        void nul_specificationNull() {
            assertThat(LotSpecifications.possedeStockDansEntrepot(null)).isNull();
        }

        @Test
        @DisplayName("ne retient que les lots ayant une ligne de stock dans l'entrepôt donné")
        void filtreLotsAvecStockDansEntrepot() {
            List<LotJpaEntity> resultats = lotJpaRepository.findAll(
                    LotSpecifications.possedeStockDansEntrepot(entrepot1));

            assertThat(resultats).extracting(LotJpaEntity::getNumeroLot).containsExactly("LOT-A-001");
        }

        @Test
        @DisplayName("entrepôt sans aucune ligne de stock → résultat vide")
        void entrepotSansStock_resultatVide() {
            List<LotJpaEntity> resultats = lotJpaRepository.findAll(
                    LotSpecifications.possedeStockDansEntrepot(entrepot2));

            assertThat(resultats).isEmpty();
        }
    }

    @Nested
    @DisplayName("combiner()")
    class Combiner {

        @Test
        @DisplayName("tous les critères null → aucun filtre, tous les lots renvoyés")
        void tousCriteresNull_aucunFiltre() {
            Specification<LotJpaEntity> spec = LotSpecifications.combiner(null, null, null, null, null);

            List<LotJpaEntity> resultats = lotJpaRepository.findAll(spec);

            assertThat(resultats).hasSize(3);
        }

        @Test
        @DisplayName("combinaison texte + statut → intersection des deux filtres")
        void combinaisonTexteEtStatut() {
            Specification<LotJpaEntity> spec = LotSpecifications.combiner("lot-a", null, null, StatutLot.ACTIF, null);

            List<LotJpaEntity> resultats = lotJpaRepository.findAll(spec);

            assertThat(resultats).extracting(LotJpaEntity::getNumeroLot).containsExactly("LOT-A-001");
        }

        @Test
        @DisplayName("combinaison de tous les critères, y compris scoping entrepôt")
        void combinaisonComplete() {
            Specification<LotJpaEntity> spec = LotSpecifications.combiner(
                    null, medicamentA, fournisseurX, StatutLot.ACTIF, entrepot1);

            List<LotJpaEntity> resultats = lotJpaRepository.findAll(spec);

            assertThat(resultats).extracting(LotJpaEntity::getNumeroLot).containsExactly("LOT-A-001");
        }

        @Test
        @DisplayName("combinaison ne correspondant à aucun lot → résultat vide")
        void combinaisonSansCorrespondance() {
            Specification<LotJpaEntity> spec = LotSpecifications.combiner(
                    null, medicamentB, fournisseurY, null, null);

            List<LotJpaEntity> resultats = lotJpaRepository.findAll(spec);

            assertThat(resultats).isEmpty();
        }
    }
}
