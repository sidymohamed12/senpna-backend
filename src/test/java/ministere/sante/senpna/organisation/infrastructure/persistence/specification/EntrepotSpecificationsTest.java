package ministere.sante.senpna.organisation.infrastructure.persistence.specification;

import ministere.sante.senpna.organisation.domain.valueobject.TypeEntrepot;
import ministere.sante.senpna.organisation.infrastructure.persistence.entity.EntrepotJpaEntity;
import ministere.sante.senpna.organisation.infrastructure.persistence.repository.EntrepotJpaRepository;

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
@DisplayName("EntrepotSpecifications — filtres JPA Specification sur les entrepôts")
class EntrepotSpecificationsTest {

    @Autowired
    EntrepotJpaRepository entrepotJpaRepository;

    UUID regionA = UUID.randomUUID();
    UUID regionB = UUID.randomUUID();

    EntrepotJpaEntity pnaCentral;
    EntrepotJpaEntity praInactif;

    @BeforeEach
    void setUp() {
        pnaCentral = new EntrepotJpaEntity(UUID.randomUUID(), "PNA-CENTRAL", "Pharmacie Nationale d'Approv.",
                TypeEntrepot.PNA_CENTRAL, regionA, null, null, null, true);
        praInactif = new EntrepotJpaEntity(UUID.randomUUID(), "PRA-DAKAR", "PRA Dakar", TypeEntrepot.PRA, regionB,
                null, null, null, false);

        entrepotJpaRepository.saveAll(List.of(pnaCentral, praInactif));
    }

    @Nested
    @DisplayName("recherche()")
    class Recherche {

        @Test
        @DisplayName("texte null ou vide → specification null")
        void texteNulOuVide_specificationNull() {
            assertThat(EntrepotSpecifications.recherche(null)).isNull();
            assertThat(EntrepotSpecifications.recherche("  ")).isNull();
        }

        @Test
        @DisplayName("filtre insensible à la casse sur le nom ou le code")
        void filtreInsensibleCasse() {
            List<EntrepotJpaEntity> resultats = entrepotJpaRepository
                    .findAll(EntrepotSpecifications.recherche("dakar"));

            assertThat(resultats).extracting(EntrepotJpaEntity::getCode).containsExactly("PRA-DAKAR");
        }

        @Test
        @DisplayName("motif sans correspondance → résultat vide")
        void motifSansCorrespondance_resultatVide() {
            assertThat(entrepotJpaRepository.findAll(EntrepotSpecifications.recherche("ZZZ-INEXISTANT"))).isEmpty();
        }
    }

    @Nested
    @DisplayName("type()")
    class TypeFiltre {

        @Test
        @DisplayName("null → specification null")
        void nul_specificationNull() {
            assertThat(EntrepotSpecifications.type(null)).isNull();
        }

        @Test
        @DisplayName("filtre uniquement les entrepôts du type donné")
        void filtreParType() {
            List<EntrepotJpaEntity> resultats = entrepotJpaRepository
                    .findAll(EntrepotSpecifications.type(TypeEntrepot.PRA));

            assertThat(resultats).extracting(EntrepotJpaEntity::getCode).containsExactly("PRA-DAKAR");
        }
    }

    @Nested
    @DisplayName("regionId()")
    class RegionIdFiltre {

        @Test
        @DisplayName("null → specification null")
        void nul_specificationNull() {
            assertThat(EntrepotSpecifications.regionId(null)).isNull();
        }

        @Test
        @DisplayName("filtre uniquement les entrepôts de la région donnée")
        void filtreParRegion() {
            List<EntrepotJpaEntity> resultats = entrepotJpaRepository
                    .findAll(EntrepotSpecifications.regionId(regionA));

            assertThat(resultats).extracting(EntrepotJpaEntity::getCode).containsExactly("PNA-CENTRAL");
        }
    }

    @Nested
    @DisplayName("actif()")
    class ActifFiltre {

        @Test
        @DisplayName("null → specification null")
        void nul_specificationNull() {
            assertThat(EntrepotSpecifications.actif(null)).isNull();
        }

        @Test
        @DisplayName("filtre uniquement les entrepôts actifs")
        void filtreEntrepotsActifs() {
            List<EntrepotJpaEntity> resultats = entrepotJpaRepository.findAll(EntrepotSpecifications.actif(true));

            assertThat(resultats).extracting(EntrepotJpaEntity::getCode).containsExactly("PNA-CENTRAL");
        }
    }

    @Nested
    @DisplayName("combiner()")
    class Combiner {

        @Test
        @DisplayName("tous les critères null → aucun filtre, tous les entrepôts renvoyés")
        void tousCriteresNull_aucunFiltre() {
            Specification<EntrepotJpaEntity> spec = EntrepotSpecifications.combiner(null, null, null, null);

            assertThat(entrepotJpaRepository.findAll(spec)).hasSize(2);
        }

        @Test
        @DisplayName("combinaison de tous les critères")
        void combinaisonComplete() {
            Specification<EntrepotJpaEntity> spec = EntrepotSpecifications.combiner("dakar",
                    TypeEntrepot.PRA, regionB, false);

            List<EntrepotJpaEntity> resultats = entrepotJpaRepository.findAll(spec);

            assertThat(resultats).extracting(EntrepotJpaEntity::getCode).containsExactly("PRA-DAKAR");
        }

        @Test
        @DisplayName("combinaison ne correspondant à aucun entrepôt → résultat vide")
        void combinaisonSansCorrespondance() {
            Specification<EntrepotJpaEntity> spec = EntrepotSpecifications.combiner("dakar", null, regionA, null);

            assertThat(entrepotJpaRepository.findAll(spec)).isEmpty();
        }
    }
}
