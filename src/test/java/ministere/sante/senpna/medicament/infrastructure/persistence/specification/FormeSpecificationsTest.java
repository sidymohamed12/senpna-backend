package ministere.sante.senpna.medicament.infrastructure.persistence.specification;

import ministere.sante.senpna.medicament.infrastructure.persistence.entity.FormeJpaEntity;
import ministere.sante.senpna.medicament.infrastructure.persistence.repository.FormeJpaRepository;

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
@DisplayName("FormeSpecifications — filtres JPA Specification sur les formes")
class FormeSpecificationsTest {

    @Autowired
    FormeJpaRepository formeJpaRepository;

    FormeJpaEntity comprime;
    FormeJpaEntity siropInactif;

    @BeforeEach
    void setUp() {
        comprime = new FormeJpaEntity(UUID.randomUUID(), "COMP", "Comprimé", "Forme solide orale", true);
        siropInactif = new FormeJpaEntity(UUID.randomUUID(), "SIROP", "Sirop", "Forme liquide orale", false);

        formeJpaRepository.saveAll(List.of(comprime, siropInactif));
    }

    @Nested
    @DisplayName("recherche()")
    class Recherche {

        @Test
        @DisplayName("texte null ou vide → specification null")
        void texteNulOuVide_specificationNull() {
            assertThat(FormeSpecifications.recherche(null)).isNull();
            assertThat(FormeSpecifications.recherche("  ")).isNull();
        }

        @Test
        @DisplayName("filtre insensible à la casse sur le code ou le libellé")
        void filtreInsensibleCasse() {
            List<FormeJpaEntity> resultats = formeJpaRepository.findAll(FormeSpecifications.recherche("comp"));

            assertThat(resultats).extracting(FormeJpaEntity::getCode).containsExactly("COMP");
        }

        @Test
        @DisplayName("motif sans correspondance → résultat vide")
        void motifSansCorrespondance_resultatVide() {
            List<FormeJpaEntity> resultats = formeJpaRepository
                    .findAll(FormeSpecifications.recherche("ZZZ-INEXISTANT"));

            assertThat(resultats).isEmpty();
        }
    }

    @Nested
    @DisplayName("actif()")
    class ActifFiltre {

        @Test
        @DisplayName("null → specification null")
        void nul_specificationNull() {
            assertThat(FormeSpecifications.actif(null)).isNull();
        }

        @Test
        @DisplayName("filtre uniquement les formes actives")
        void filtreFormesActives() {
            List<FormeJpaEntity> resultats = formeJpaRepository.findAll(FormeSpecifications.actif(true));

            assertThat(resultats).extracting(FormeJpaEntity::getCode).containsExactly("COMP");
        }

        @Test
        @DisplayName("filtre uniquement les formes archivées")
        void filtreFormesArchivees() {
            List<FormeJpaEntity> resultats = formeJpaRepository.findAll(FormeSpecifications.actif(false));

            assertThat(resultats).extracting(FormeJpaEntity::getCode).containsExactly("SIROP");
        }
    }

    @Nested
    @DisplayName("combiner()")
    class Combiner {

        @Test
        @DisplayName("tous les critères null → aucun filtre, toutes les formes renvoyées")
        void tousCriteresNull_aucunFiltre() {
            Specification<FormeJpaEntity> spec = FormeSpecifications.combiner(null, null);

            assertThat(formeJpaRepository.findAll(spec)).hasSize(2);
        }

        @Test
        @DisplayName("combinaison texte + actif → intersection des deux filtres")
        void combinaisonTexteEtActif() {
            Specification<FormeJpaEntity> spec = FormeSpecifications.combiner("comp", true);

            List<FormeJpaEntity> resultats = formeJpaRepository.findAll(spec);

            assertThat(resultats).extracting(FormeJpaEntity::getCode).containsExactly("COMP");
        }

        @Test
        @DisplayName("combinaison ne correspondant à aucune forme → résultat vide")
        void combinaisonSansCorrespondance() {
            Specification<FormeJpaEntity> spec = FormeSpecifications.combiner("comp", false);

            assertThat(formeJpaRepository.findAll(spec)).isEmpty();
        }
    }
}
