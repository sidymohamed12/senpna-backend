package ministere.sante.senpna.medicament.infrastructure.persistence.specification;

import ministere.sante.senpna.medicament.infrastructure.persistence.entity.FamilleJpaEntity;
import ministere.sante.senpna.medicament.infrastructure.persistence.repository.FamilleJpaRepository;

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
@DisplayName("FamilleSpecifications — filtres JPA Specification sur les familles")
class FamilleSpecificationsTest {

    @Autowired
    FamilleJpaRepository familleJpaRepository;

    FamilleJpaEntity antibiotiques;
    FamilleJpaEntity antalgiquesInactive;

    @BeforeEach
    void setUp() {
        antibiotiques = new FamilleJpaEntity(UUID.randomUUID(), "ANTIBIO", "Antibiotiques", "Description", true);
        antalgiquesInactive = new FamilleJpaEntity(UUID.randomUUID(), "ANTALG", "Antalgiques", "Description", false);

        familleJpaRepository.saveAll(List.of(antibiotiques, antalgiquesInactive));
    }

    @Nested
    @DisplayName("recherche()")
    class Recherche {

        @Test
        @DisplayName("texte null ou vide → specification null")
        void texteNulOuVide_specificationNull() {
            assertThat(FamilleSpecifications.recherche(null)).isNull();
            assertThat(FamilleSpecifications.recherche("  ")).isNull();
        }

        @Test
        @DisplayName("filtre insensible à la casse sur le code ou le libellé")
        void filtreInsensibleCasse() {
            List<FamilleJpaEntity> resultats = familleJpaRepository.findAll(FamilleSpecifications.recherche("antib"));

            assertThat(resultats).extracting(FamilleJpaEntity::getCode).containsExactly("ANTIBIO");
        }

        @Test
        @DisplayName("motif sans correspondance → résultat vide")
        void motifSansCorrespondance_resultatVide() {
            List<FamilleJpaEntity> resultats = familleJpaRepository
                    .findAll(FamilleSpecifications.recherche("ZZZ-INEXISTANT"));

            assertThat(resultats).isEmpty();
        }
    }

    @Nested
    @DisplayName("actif()")
    class ActifFiltre {

        @Test
        @DisplayName("null → specification null")
        void nul_specificationNull() {
            assertThat(FamilleSpecifications.actif(null)).isNull();
        }

        @Test
        @DisplayName("filtre uniquement les familles actives")
        void filtreFamillesActives() {
            List<FamilleJpaEntity> resultats = familleJpaRepository.findAll(FamilleSpecifications.actif(true));

            assertThat(resultats).extracting(FamilleJpaEntity::getCode).containsExactly("ANTIBIO");
        }

        @Test
        @DisplayName("filtre uniquement les familles archivées")
        void filtreFamillesArchivees() {
            List<FamilleJpaEntity> resultats = familleJpaRepository.findAll(FamilleSpecifications.actif(false));

            assertThat(resultats).extracting(FamilleJpaEntity::getCode).containsExactly("ANTALG");
        }
    }

    @Nested
    @DisplayName("combiner()")
    class Combiner {

        @Test
        @DisplayName("tous les critères null → aucun filtre, toutes les familles renvoyées")
        void tousCriteresNull_aucunFiltre() {
            Specification<FamilleJpaEntity> spec = FamilleSpecifications.combiner(null, null);

            assertThat(familleJpaRepository.findAll(spec)).hasSize(2);
        }

        @Test
        @DisplayName("combinaison texte + actif → intersection des deux filtres")
        void combinaisonTexteEtActif() {
            Specification<FamilleJpaEntity> spec = FamilleSpecifications.combiner("anti", true);

            List<FamilleJpaEntity> resultats = familleJpaRepository.findAll(spec);

            assertThat(resultats).extracting(FamilleJpaEntity::getCode).containsExactly("ANTIBIO");
        }

        @Test
        @DisplayName("combinaison ne correspondant à aucune famille → résultat vide")
        void combinaisonSansCorrespondance() {
            Specification<FamilleJpaEntity> spec = FamilleSpecifications.combiner("antibio", false);

            assertThat(familleJpaRepository.findAll(spec)).isEmpty();
        }
    }
}
