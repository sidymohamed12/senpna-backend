package ministere.sante.senpna.medicament.infrastructure.persistence.specification;

import ministere.sante.senpna.medicament.infrastructure.persistence.entity.ConditionnementJpaEntity;
import ministere.sante.senpna.medicament.infrastructure.persistence.repository.ConditionnementJpaRepository;

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
@DisplayName("ConditionnementSpecifications — filtres JPA Specification sur les conditionnements")
class ConditionnementSpecificationsTest {

    @Autowired
    ConditionnementJpaRepository conditionnementJpaRepository;

    UUID medicamentA = UUID.randomUUID();
    UUID medicamentB = UUID.randomUUID();

    ConditionnementJpaEntity uniteBaseA;
    ConditionnementJpaEntity boiteInactiveB;

    @BeforeEach
    void setUp() {
        uniteBaseA = ConditionnementJpaEntity.builder()
            .id(UUID.randomUUID())
            .medicamentId(medicamentA)
            .nom("Comprimé")
            .niveau(0)
            .quantiteUniteBase(BigDecimal.ONE)
            .estUniteBase(true)
            .prixAchat(BigDecimal.TEN)
            .prixVente(new BigDecimal("15"))
            .actif(true)
            .build();
        boiteInactiveB = ConditionnementJpaEntity.builder()
            .id(UUID.randomUUID())
            .medicamentId(medicamentB)
            .nom("Boîte de 20")
            .niveau(1)
            .quantiteUniteBase(BigDecimal.valueOf(20))
            .estUniteBase(false)
            .prixAchat(BigDecimal.valueOf(200))
            .prixVente(BigDecimal.valueOf(300))
            .actif(false)
            .build();

        conditionnementJpaRepository.saveAll(List.of(uniteBaseA, boiteInactiveB));
    }

    @Nested
    @DisplayName("medicamentId()")
    class MedicamentIdFiltre {

        @Test
        @DisplayName("null → specification null")
        void nul_specificationNull() {
            assertThat(ConditionnementSpecifications.medicamentId(null)).isNull();
        }

        @Test
        @DisplayName("filtre uniquement les conditionnements du médicament donné")
        void filtreConditionnementsDuMedicament() {
            List<ConditionnementJpaEntity> resultats = conditionnementJpaRepository
                    .findAll(ConditionnementSpecifications.medicamentId(medicamentA));

            assertThat(resultats).extracting(ConditionnementJpaEntity::getNom).containsExactly("Comprimé");
        }
    }

    @Nested
    @DisplayName("actif()")
    class ActifFiltre {

        @Test
        @DisplayName("null → specification null")
        void nul_specificationNull() {
            assertThat(ConditionnementSpecifications.actif(null)).isNull();
        }

        @Test
        @DisplayName("filtre uniquement les conditionnements actifs")
        void filtreConditionnementsActifs() {
            List<ConditionnementJpaEntity> resultats = conditionnementJpaRepository
                    .findAll(ConditionnementSpecifications.actif(true));

            assertThat(resultats).extracting(ConditionnementJpaEntity::getNom).containsExactly("Comprimé");
        }

        @Test
        @DisplayName("filtre uniquement les conditionnements archivés")
        void filtreConditionnementsArchives() {
            List<ConditionnementJpaEntity> resultats = conditionnementJpaRepository
                    .findAll(ConditionnementSpecifications.actif(false));

            assertThat(resultats).extracting(ConditionnementJpaEntity::getNom).containsExactly("Boîte de 20");
        }
    }

    @Nested
    @DisplayName("combiner()")
    class Combiner {

        @Test
        @DisplayName("tous les critères null → aucun filtre, tous les conditionnements renvoyés")
        void tousCriteresNull_aucunFiltre() {
            Specification<ConditionnementJpaEntity> spec = ConditionnementSpecifications.combiner(null, null);

            assertThat(conditionnementJpaRepository.findAll(spec)).hasSize(2);
        }

        @Test
        @DisplayName("combinaison medicamentId + actif → intersection des deux filtres")
        void combinaisonMedicamentEtActif() {
            Specification<ConditionnementJpaEntity> spec = ConditionnementSpecifications.combiner(medicamentA, true);

            List<ConditionnementJpaEntity> resultats = conditionnementJpaRepository.findAll(spec);

            assertThat(resultats).extracting(ConditionnementJpaEntity::getNom).containsExactly("Comprimé");
        }

        @Test
        @DisplayName("combinaison ne correspondant à aucun conditionnement → résultat vide")
        void combinaisonSansCorrespondance() {
            Specification<ConditionnementJpaEntity> spec = ConditionnementSpecifications.combiner(medicamentA,
                    false);

            assertThat(conditionnementJpaRepository.findAll(spec)).isEmpty();
        }
    }
}
