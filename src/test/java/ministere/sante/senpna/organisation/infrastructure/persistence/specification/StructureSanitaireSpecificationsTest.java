package ministere.sante.senpna.organisation.infrastructure.persistence.specification;

import ministere.sante.senpna.organisation.domain.valueobject.StatutAdhesion;
import ministere.sante.senpna.organisation.domain.valueobject.TypeStructureSanitaire;
import ministere.sante.senpna.organisation.infrastructure.persistence.entity.StructureSanitaireJpaEntity;
import ministere.sante.senpna.organisation.infrastructure.persistence.repository.StructureSanitaireJpaRepository;

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
@DisplayName("StructureSanitaireSpecifications — filtres JPA Specification sur les structures sanitaires")
class StructureSanitaireSpecificationsTest {

    @Autowired
    StructureSanitaireJpaRepository structureSanitaireJpaRepository;

    UUID regionA = UUID.randomUUID();
    UUID regionB = UUID.randomUUID();
    UUID praA = UUID.randomUUID();
    UUID praB = UUID.randomUUID();

    StructureSanitaireJpaEntity hopitalValide;
    StructureSanitaireJpaEntity centreInactifEnAttente;

    @BeforeEach
    void setUp() {
        hopitalValide = new StructureSanitaireJpaEntity(UUID.randomUUID(), "HOP-DKR", "Hôpital de Dakar",
                TypeStructureSanitaire.HOPITAL, regionA, praA, null, null, null, null, null, null,
                StatutAdhesion.VALIDEE, null, true);
        centreInactifEnAttente = new StructureSanitaireJpaEntity(UUID.randomUUID(), "CS-THIES", "Centre de Thiès",
                TypeStructureSanitaire.CENTRE_SANTE, regionB, praB, null, null, null, null, null, null,
                StatutAdhesion.EN_ATTENTE_VALIDATION, null, false);

        structureSanitaireJpaRepository.saveAll(List.of(hopitalValide, centreInactifEnAttente));
    }

    @Nested
    @DisplayName("recherche()")
    class Recherche {

        @Test
        @DisplayName("texte null ou vide → specification null")
        void texteNulOuVide_specificationNull() {
            assertThat(StructureSanitaireSpecifications.recherche(null)).isNull();
            assertThat(StructureSanitaireSpecifications.recherche("  ")).isNull();
        }

        @Test
        @DisplayName("filtre insensible à la casse sur le nom ou le code")
        void filtreInsensibleCasse() {
            List<StructureSanitaireJpaEntity> resultats = structureSanitaireJpaRepository
                    .findAll(StructureSanitaireSpecifications.recherche("thies"));

            assertThat(resultats).extracting(StructureSanitaireJpaEntity::getCode).containsExactly("CS-THIES");
        }

        @Test
        @DisplayName("motif sans correspondance → résultat vide")
        void motifSansCorrespondance_resultatVide() {
            assertThat(structureSanitaireJpaRepository
                    .findAll(StructureSanitaireSpecifications.recherche("ZZZ-INEXISTANT"))).isEmpty();
        }
    }

    @Nested
    @DisplayName("type()")
    class TypeFiltre {

        @Test
        @DisplayName("null → specification null")
        void nul_specificationNull() {
            assertThat(StructureSanitaireSpecifications.type(null)).isNull();
        }

        @Test
        @DisplayName("filtre uniquement les structures du type donné")
        void filtreParType() {
            List<StructureSanitaireJpaEntity> resultats = structureSanitaireJpaRepository
                    .findAll(StructureSanitaireSpecifications.type(TypeStructureSanitaire.HOPITAL));

            assertThat(resultats).extracting(StructureSanitaireJpaEntity::getCode).containsExactly("HOP-DKR");
        }
    }

    @Nested
    @DisplayName("regionId()")
    class RegionIdFiltre {

        @Test
        @DisplayName("null → specification null")
        void nul_specificationNull() {
            assertThat(StructureSanitaireSpecifications.regionId(null)).isNull();
        }

        @Test
        @DisplayName("filtre uniquement les structures de la région donnée")
        void filtreParRegion() {
            List<StructureSanitaireJpaEntity> resultats = structureSanitaireJpaRepository
                    .findAll(StructureSanitaireSpecifications.regionId(regionB));

            assertThat(resultats).extracting(StructureSanitaireJpaEntity::getCode).containsExactly("CS-THIES");
        }
    }

    @Nested
    @DisplayName("praId()")
    class PraIdFiltre {

        @Test
        @DisplayName("null → specification null")
        void nul_specificationNull() {
            assertThat(StructureSanitaireSpecifications.praId(null)).isNull();
        }

        @Test
        @DisplayName("filtre uniquement les structures du PRA donné")
        void filtreParPra() {
            List<StructureSanitaireJpaEntity> resultats = structureSanitaireJpaRepository
                    .findAll(StructureSanitaireSpecifications.praId(praA));

            assertThat(resultats).extracting(StructureSanitaireJpaEntity::getCode).containsExactly("HOP-DKR");
        }
    }

    @Nested
    @DisplayName("statutAdhesion()")
    class StatutAdhesionFiltre {

        @Test
        @DisplayName("null → specification null")
        void nul_specificationNull() {
            assertThat(StructureSanitaireSpecifications.statutAdhesion(null)).isNull();
        }

        @Test
        @DisplayName("filtre uniquement les structures du statut d'adhésion donné")
        void filtreParStatutAdhesion() {
            List<StructureSanitaireJpaEntity> resultats = structureSanitaireJpaRepository
                    .findAll(StructureSanitaireSpecifications.statutAdhesion(StatutAdhesion.EN_ATTENTE_VALIDATION));

            assertThat(resultats).extracting(StructureSanitaireJpaEntity::getCode).containsExactly("CS-THIES");
        }
    }

    @Nested
    @DisplayName("actif()")
    class ActifFiltre {

        @Test
        @DisplayName("null → specification null")
        void nul_specificationNull() {
            assertThat(StructureSanitaireSpecifications.actif(null)).isNull();
        }

        @Test
        @DisplayName("filtre uniquement les structures actives")
        void filtreStructuresActives() {
            List<StructureSanitaireJpaEntity> resultats = structureSanitaireJpaRepository
                    .findAll(StructureSanitaireSpecifications.actif(true));

            assertThat(resultats).extracting(StructureSanitaireJpaEntity::getCode).containsExactly("HOP-DKR");
        }
    }

    @Nested
    @DisplayName("combiner()")
    class Combiner {

        @Test
        @DisplayName("tous les critères null → aucun filtre, toutes les structures renvoyées")
        void tousCriteresNull_aucunFiltre() {
            Specification<StructureSanitaireJpaEntity> spec = StructureSanitaireSpecifications.combiner(null, null,
                    null, null, null, null);

            assertThat(structureSanitaireJpaRepository.findAll(spec)).hasSize(2);
        }

        @Test
        @DisplayName("combinaison de tous les critères")
        void combinaisonComplete() {
            Specification<StructureSanitaireJpaEntity> spec = StructureSanitaireSpecifications.combiner("dkr",
                    TypeStructureSanitaire.HOPITAL, regionA, praA, StatutAdhesion.VALIDEE, true);

            List<StructureSanitaireJpaEntity> resultats = structureSanitaireJpaRepository.findAll(spec);

            assertThat(resultats).extracting(StructureSanitaireJpaEntity::getCode).containsExactly("HOP-DKR");
        }

        @Test
        @DisplayName("combinaison ne correspondant à aucune structure → résultat vide")
        void combinaisonSansCorrespondance() {
            Specification<StructureSanitaireJpaEntity> spec = StructureSanitaireSpecifications.combiner("dkr", null,
                    regionA, null, StatutAdhesion.REJETEE, null);

            assertThat(structureSanitaireJpaRepository.findAll(spec)).isEmpty();
        }
    }
}
