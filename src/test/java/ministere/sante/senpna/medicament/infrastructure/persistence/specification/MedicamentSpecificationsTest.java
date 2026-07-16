package ministere.sante.senpna.medicament.infrastructure.persistence.specification;

import ministere.sante.senpna.medicament.infrastructure.persistence.entity.MedicamentJpaEntity;
import ministere.sante.senpna.medicament.infrastructure.persistence.repository.MedicamentJpaRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("MedicamentSpecifications — filtres JPA Specification sur les médicaments")
class MedicamentSpecificationsTest {

    @Autowired
    MedicamentJpaRepository medicamentJpaRepository;

    UUID familleA = UUID.randomUUID();
    UUID familleB = UUID.randomUUID();
    UUID formeA = UUID.randomUUID();
    UUID formeB = UUID.randomUUID();

    MedicamentJpaEntity doliprane;
    MedicamentJpaEntity amoxicillineInactif;

    @BeforeEach
    void setUp() {
        doliprane = new MedicamentJpaEntity(UUID.randomUUID(), "PARA500", "Doliprane", "Paracétamol", "500mg",
                formeA, familleA, null, null, null, null, false, "Sanofi", null, null, true);
        amoxicillineInactif = new MedicamentJpaEntity(UUID.randomUUID(), "AMOX", "Amoxicilline", "Amoxicilline",
                "1g", formeB, familleB, null, null, null, null, false, "Pfizer", null, null, false);

        medicamentJpaRepository.saveAll(List.of(doliprane, amoxicillineInactif));
    }

    @Nested
    @DisplayName("recherche()")
    class Recherche {

        @Test
        @DisplayName("texte null ou vide → specification null")
        void texteNulOuVide_specificationNull() {
            assertThat(MedicamentSpecifications.recherche(null)).isNull();
            assertThat(MedicamentSpecifications.recherche("  ")).isNull();
        }

        @Test
        @DisplayName("filtre insensible à la casse sur le code, le nom commercial ou la DCI")
        void filtreInsensibleCasse() {
            List<MedicamentJpaEntity> resultats = medicamentJpaRepository
                    .findAll(MedicamentSpecifications.recherche("dolipr"));

            assertThat(resultats).extracting(MedicamentJpaEntity::getCode).containsExactly("PARA500");
        }

        @Test
        @DisplayName("filtre sur la DCI")
        void filtreSurDci() {
            List<MedicamentJpaEntity> resultats = medicamentJpaRepository
                    .findAll(MedicamentSpecifications.recherche("amoxicilline"));

            assertThat(resultats).extracting(MedicamentJpaEntity::getCode).containsExactly("AMOX");
        }

        @Test
        @DisplayName("motif sans correspondance → résultat vide")
        void motifSansCorrespondance_resultatVide() {
            List<MedicamentJpaEntity> resultats = medicamentJpaRepository
                    .findAll(MedicamentSpecifications.recherche("ZZZ-INEXISTANT"));

            assertThat(resultats).isEmpty();
        }
    }

    @Nested
    @DisplayName("familleId()")
    class FamilleIdFiltre {

        @Test
        @DisplayName("null → specification null")
        void nul_specificationNull() {
            assertThat(MedicamentSpecifications.familleId(null)).isNull();
        }

        @Test
        @DisplayName("filtre uniquement les médicaments de la famille donnée")
        void filtreMedicamentsDeLaFamille() {
            List<MedicamentJpaEntity> resultats = medicamentJpaRepository
                    .findAll(MedicamentSpecifications.familleId(familleB));

            assertThat(resultats).extracting(MedicamentJpaEntity::getCode).containsExactly("AMOX");
        }
    }

    @Nested
    @DisplayName("formeId()")
    class FormeIdFiltre {

        @Test
        @DisplayName("null → specification null")
        void nul_specificationNull() {
            assertThat(MedicamentSpecifications.formeId(null)).isNull();
        }

        @Test
        @DisplayName("filtre uniquement les médicaments de la forme donnée")
        void filtreMedicamentsDeLaForme() {
            List<MedicamentJpaEntity> resultats = medicamentJpaRepository
                    .findAll(MedicamentSpecifications.formeId(formeA));

            assertThat(resultats).extracting(MedicamentJpaEntity::getCode).containsExactly("PARA500");
        }
    }

    @Nested
    @DisplayName("actif()")
    class ActifFiltre {

        @Test
        @DisplayName("null → specification null")
        void nul_specificationNull() {
            assertThat(MedicamentSpecifications.actif(null)).isNull();
        }

        @Test
        @DisplayName("filtre uniquement les médicaments actifs")
        void filtreMedicamentsActifs() {
            List<MedicamentJpaEntity> resultats = medicamentJpaRepository
                    .findAll(MedicamentSpecifications.actif(true));

            assertThat(resultats).extracting(MedicamentJpaEntity::getCode).containsExactly("PARA500");
        }
    }

    @Nested
    @DisplayName("combiner()")
    class Combiner {

        @Test
        @DisplayName("tous les critères null → aucun filtre, tous les médicaments renvoyés")
        void tousCriteresNull_aucunFiltre() {
            Specification<MedicamentJpaEntity> spec = MedicamentSpecifications.combiner(null, null, null, null);

            assertThat(medicamentJpaRepository.findAll(spec)).hasSize(2);
        }

        @Test
        @DisplayName("combinaison de tous les critères")
        void combinaisonComplete() {
            Specification<MedicamentJpaEntity> spec = MedicamentSpecifications.combiner("dolipr", familleA, formeA,
                    true);

            List<MedicamentJpaEntity> resultats = medicamentJpaRepository.findAll(spec);

            assertThat(resultats).extracting(MedicamentJpaEntity::getCode).containsExactly("PARA500");
        }

        @Test
        @DisplayName("combinaison ne correspondant à aucun médicament → résultat vide")
        void combinaisonSansCorrespondance() {
            Specification<MedicamentJpaEntity> spec = MedicamentSpecifications.combiner("dolipr", familleB, null,
                    null);

            assertThat(medicamentJpaRepository.findAll(spec)).isEmpty();
        }
    }
}
