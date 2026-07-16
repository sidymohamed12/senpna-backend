package ministere.sante.senpna.stock.infrastructure.persistence.specification;

import ministere.sante.senpna.stock.domain.valueobject.SensMouvement;
import ministere.sante.senpna.stock.domain.valueobject.TypeMouvement;
import ministere.sante.senpna.stock.infrastructure.persistence.entity.MouvementStockJpaEntity;
import ministere.sante.senpna.stock.infrastructure.persistence.repository.MouvementStockJpaRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("MouvementStockSpecifications — filtres JPA Specification sur les mouvements de stock")
class MouvementStockSpecificationsTest {

    @Autowired
    MouvementStockJpaRepository mouvementStockJpaRepository;

    UUID lotA = UUID.randomUUID();
    UUID lotB = UUID.randomUUID();
    UUID medicamentA = UUID.randomUUID();
    UUID medicamentB = UUID.randomUUID();
    UUID entrepotSource = UUID.randomUUID();
    UUID entrepotDestination = UUID.randomUUID();
    UUID utilisateurA = UUID.randomUUID();
    UUID utilisateurB = UUID.randomUUID();

    Instant ilYA10Jours = Instant.now().minus(10, ChronoUnit.DAYS);
    Instant ilYA5Jours = Instant.now().minus(5, ChronoUnit.DAYS);
    Instant maintenant = Instant.now();

    MouvementStockJpaEntity entree;
    MouvementStockJpaEntity sortie;

    @BeforeEach
    void setUp() {
        entree = new MouvementStockJpaEntity(UUID.randomUUID(), TypeMouvement.ENTREE_ACHAT.name(),
                SensMouvement.ENTREE.name(), null, entrepotDestination, null, lotA, medicamentA, BigDecimal.TEN,
                ilYA10Jours, "REF-1", null, utilisateurA);
        sortie = new MouvementStockJpaEntity(UUID.randomUUID(), TypeMouvement.SORTIE_STRUCTURE.name(),
                SensMouvement.SORTIE.name(), entrepotSource, null, null, lotB, medicamentB, BigDecimal.ONE,
                maintenant, "REF-2", "Livraison", utilisateurB);

        mouvementStockJpaRepository.saveAll(List.of(entree, sortie));
    }

    @Nested
    @DisplayName("lotId()")
    class LotIdFiltre {

        @Test
        @DisplayName("null → specification null")
        void nul_specificationNull() {
            assertThat(MouvementStockSpecifications.lotId(null)).isNull();
        }

        @Test
        @DisplayName("filtre uniquement les mouvements du lot donné")
        void filtreParLot() {
            List<MouvementStockJpaEntity> resultats = mouvementStockJpaRepository
                    .findAll(MouvementStockSpecifications.lotId(lotA));

            assertThat(resultats).extracting(MouvementStockJpaEntity::getLotId).containsExactly(lotA);
        }
    }

    @Nested
    @DisplayName("medicamentId()")
    class MedicamentIdFiltre {

        @Test
        @DisplayName("null → specification null")
        void nul_specificationNull() {
            assertThat(MouvementStockSpecifications.medicamentId(null)).isNull();
        }

        @Test
        @DisplayName("filtre uniquement les mouvements du médicament donné")
        void filtreParMedicament() {
            List<MouvementStockJpaEntity> resultats = mouvementStockJpaRepository
                    .findAll(MouvementStockSpecifications.medicamentId(medicamentB));

            assertThat(resultats).extracting(MouvementStockJpaEntity::getMedicamentId).containsExactly(medicamentB);
        }
    }

    @Nested
    @DisplayName("entrepotId()")
    class EntrepotIdFiltre {

        @Test
        @DisplayName("null → specification null")
        void nul_specificationNull() {
            assertThat(MouvementStockSpecifications.entrepotId(null)).isNull();
        }

        @Test
        @DisplayName("retrouve un mouvement où l'entrepôt intervient comme source")
        void filtreParEntrepotSource() {
            List<MouvementStockJpaEntity> resultats = mouvementStockJpaRepository
                    .findAll(MouvementStockSpecifications.entrepotId(entrepotSource));

            assertThat(resultats).extracting(MouvementStockJpaEntity::getLotId).containsExactly(lotB);
        }

        @Test
        @DisplayName("retrouve un mouvement où l'entrepôt intervient comme destination")
        void filtreParEntrepotDestination() {
            List<MouvementStockJpaEntity> resultats = mouvementStockJpaRepository
                    .findAll(MouvementStockSpecifications.entrepotId(entrepotDestination));

            assertThat(resultats).extracting(MouvementStockJpaEntity::getLotId).containsExactly(lotA);
        }
    }

    @Nested
    @DisplayName("typeMouvement()")
    class TypeMouvementFiltre {

        @Test
        @DisplayName("null → specification null")
        void nul_specificationNull() {
            assertThat(MouvementStockSpecifications.typeMouvement(null)).isNull();
        }

        @Test
        @DisplayName("filtre uniquement les mouvements du type donné")
        void filtreParType() {
            List<MouvementStockJpaEntity> resultats = mouvementStockJpaRepository
                    .findAll(MouvementStockSpecifications.typeMouvement(TypeMouvement.SORTIE_STRUCTURE));

            assertThat(resultats).extracting(MouvementStockJpaEntity::getLotId).containsExactly(lotB);
        }
    }

    @Nested
    @DisplayName("sens()")
    class SensFiltre {

        @Test
        @DisplayName("null → specification null")
        void nul_specificationNull() {
            assertThat(MouvementStockSpecifications.sens(null)).isNull();
        }

        @Test
        @DisplayName("filtre uniquement les mouvements du sens donné")
        void filtreParSens() {
            List<MouvementStockJpaEntity> resultats = mouvementStockJpaRepository
                    .findAll(MouvementStockSpecifications.sens(SensMouvement.ENTREE));

            assertThat(resultats).extracting(MouvementStockJpaEntity::getLotId).containsExactly(lotA);
        }
    }

    @Nested
    @DisplayName("utilisateurId()")
    class UtilisateurIdFiltre {

        @Test
        @DisplayName("null → specification null")
        void nul_specificationNull() {
            assertThat(MouvementStockSpecifications.utilisateurId(null)).isNull();
        }

        @Test
        @DisplayName("filtre uniquement les mouvements de l'utilisateur donné")
        void filtreParUtilisateur() {
            List<MouvementStockJpaEntity> resultats = mouvementStockJpaRepository
                    .findAll(MouvementStockSpecifications.utilisateurId(utilisateurB));

            assertThat(resultats).extracting(MouvementStockJpaEntity::getLotId).containsExactly(lotB);
        }
    }

    @Nested
    @DisplayName("periode()")
    class PeriodeFiltre {

        @Test
        @DisplayName("dateDebut et dateFin toutes deux null → specification null")
        void toutesDeuxNulles_specificationNull() {
            assertThat(MouvementStockSpecifications.periode(null, null)).isNull();
        }

        @Test
        @DisplayName("dateDebut et dateFin fournies → intervalle inclusif")
        void intervalleComplet() {
            List<MouvementStockJpaEntity> resultats = mouvementStockJpaRepository.findAll(
                    MouvementStockSpecifications.periode(ilYA10Jours.minusSeconds(1), ilYA5Jours));

            assertThat(resultats).extracting(MouvementStockJpaEntity::getLotId).containsExactly(lotA);
        }

        @Test
        @DisplayName("seulement dateDebut → mouvements à partir de cette date")
        void seulementDateDebut() {
            List<MouvementStockJpaEntity> resultats = mouvementStockJpaRepository
                    .findAll(MouvementStockSpecifications.periode(ilYA5Jours, null));

            assertThat(resultats).extracting(MouvementStockJpaEntity::getLotId).containsExactly(lotB);
        }

        @Test
        @DisplayName("seulement dateFin → mouvements jusqu'à cette date")
        void seulementDateFin() {
            List<MouvementStockJpaEntity> resultats = mouvementStockJpaRepository
                    .findAll(MouvementStockSpecifications.periode(null, ilYA5Jours));

            assertThat(resultats).extracting(MouvementStockJpaEntity::getLotId).containsExactly(lotA);
        }
    }

    @Nested
    @DisplayName("combiner()")
    class Combiner {

        @Test
        @DisplayName("tous les critères null → aucun filtre, tous les mouvements renvoyés")
        void tousCriteresNull_aucunFiltre() {
            Specification<MouvementStockJpaEntity> spec = MouvementStockSpecifications.combiner(null, null, null,
                    null, null, null, null, null);

            assertThat(mouvementStockJpaRepository.findAll(spec)).hasSize(2);
        }

        @Test
        @DisplayName("combinaison de tous les critères")
        void combinaisonComplete() {
            Specification<MouvementStockJpaEntity> spec = MouvementStockSpecifications.combiner(lotA, medicamentA,
                    entrepotDestination, TypeMouvement.ENTREE_ACHAT, SensMouvement.ENTREE, utilisateurA,
                    ilYA10Jours.minusSeconds(1), ilYA10Jours.plusSeconds(1));

            List<MouvementStockJpaEntity> resultats = mouvementStockJpaRepository.findAll(spec);

            assertThat(resultats).extracting(MouvementStockJpaEntity::getLotId).containsExactly(lotA);
        }

        @Test
        @DisplayName("combinaison ne correspondant à aucun mouvement → résultat vide")
        void combinaisonSansCorrespondance() {
            Specification<MouvementStockJpaEntity> spec = MouvementStockSpecifications.combiner(lotA, null, null,
                    TypeMouvement.SORTIE_STRUCTURE, null, null, null, null);

            assertThat(mouvementStockJpaRepository.findAll(spec)).isEmpty();
        }
    }
}
