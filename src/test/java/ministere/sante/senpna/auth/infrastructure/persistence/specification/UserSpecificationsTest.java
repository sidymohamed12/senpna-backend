package ministere.sante.senpna.auth.infrastructure.persistence.specification;

import ministere.sante.senpna.auth.infrastructure.persistence.entity.UserJpaEntity;
import ministere.sante.senpna.auth.infrastructure.persistence.repository.UserJpaRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("UserSpecifications — filtres JPA Specification sur les utilisateurs")
class UserSpecificationsTest {

    @Autowired
    UserJpaRepository userJpaRepository;

    UUID roleAdmin = UUID.randomUUID();
    UUID roleGestionnaire = UUID.randomUUID();

    UserJpaEntity awaActive;
    UserJpaEntity soleneInactive;

    @BeforeEach
    void setUp() {
        awaActive = UserJpaEntity.builder()
            .id(UUID.randomUUID())
            .nom("Diallo")
            .prenom("Awa")
            .email("awa.diallo@example.com")
            .telephone(null)
            .passwordHash("hash")
            .actif(true)
            .roleIds(Set.of(roleAdmin))
            .tentativesEchecConnexion(0)
            .verrouilleJusqua(null)
            .build();
        soleneInactive = UserJpaEntity.builder()
            .id(UUID.randomUUID())
            .nom("Sow")
            .prenom("Solene")
            .email("solene.sow@example.com")
            .telephone(null)
            .passwordHash("hash")
            .actif(false)
            .roleIds(Set.of(roleGestionnaire))
            .tentativesEchecConnexion(0)
            .verrouilleJusqua(null)
            .build();

        userJpaRepository.saveAll(List.of(awaActive, soleneInactive));
    }

    @Nested
    @DisplayName("recherche()")
    class Recherche {

        @Test
        @DisplayName("texte null ou vide → specification null")
        void texteNulOuVide_specificationNull() {
            assertThat(UserSpecifications.recherche(null)).isNull();
            assertThat(UserSpecifications.recherche("  ")).isNull();
        }

        @Test
        @DisplayName("filtre insensible à la casse sur le nom")
        void filtreParNom() {
            List<UserJpaEntity> resultats = userJpaRepository.findAll(UserSpecifications.recherche("diallo"));

            assertThat(resultats).extracting(UserJpaEntity::getEmail).containsExactly("awa.diallo@example.com");
        }

        @Test
        @DisplayName("filtre insensible à la casse sur le prénom")
        void filtreParPrenom() {
            List<UserJpaEntity> resultats = userJpaRepository.findAll(UserSpecifications.recherche("SOLENE"));

            assertThat(resultats).extracting(UserJpaEntity::getEmail).containsExactly("solene.sow@example.com");
        }

        @Test
        @DisplayName("filtre insensible à la casse sur l'email")
        void filtreParEmail() {
            List<UserJpaEntity> resultats = userJpaRepository.findAll(UserSpecifications.recherche("awa.diallo"));

            assertThat(resultats).extracting(UserJpaEntity::getEmail).containsExactly("awa.diallo@example.com");
        }

        @Test
        @DisplayName("motif sans correspondance → résultat vide")
        void motifSansCorrespondance_resultatVide() {
            assertThat(userJpaRepository.findAll(UserSpecifications.recherche("ZZZ-INEXISTANT"))).isEmpty();
        }
    }

    @Nested
    @DisplayName("actif()")
    class ActifFiltre {

        @Test
        @DisplayName("null → specification null")
        void nul_specificationNull() {
            assertThat(UserSpecifications.actif(null)).isNull();
        }

        @Test
        @DisplayName("filtre uniquement les utilisateurs actifs")
        void filtreUtilisateursActifs() {
            List<UserJpaEntity> resultats = userJpaRepository.findAll(UserSpecifications.actif(true));

            assertThat(resultats).extracting(UserJpaEntity::getEmail).containsExactly("awa.diallo@example.com");
        }

        @Test
        @DisplayName("filtre uniquement les utilisateurs inactifs")
        void filtreUtilisateursInactifs() {
            List<UserJpaEntity> resultats = userJpaRepository.findAll(UserSpecifications.actif(false));

            assertThat(resultats).extracting(UserJpaEntity::getEmail).containsExactly("solene.sow@example.com");
        }
    }

    @Nested
    @DisplayName("possedeRole()")
    class PossedeRoleFiltre {

        @Test
        @DisplayName("null → specification null")
        void nul_specificationNull() {
            assertThat(UserSpecifications.possedeRole(null)).isNull();
        }

        @Test
        @DisplayName("filtre uniquement les utilisateurs possédant le rôle donné")
        void filtreUtilisateursAvecRole() {
            List<UserJpaEntity> resultats = userJpaRepository.findAll(UserSpecifications.possedeRole(roleAdmin));

            assertThat(resultats).extracting(UserJpaEntity::getEmail).containsExactly("awa.diallo@example.com");
        }

        @Test
        @DisplayName("rôle sans utilisateur → résultat vide")
        void roleSansUtilisateur_resultatVide() {
            assertThat(userJpaRepository.findAll(UserSpecifications.possedeRole(UUID.randomUUID()))).isEmpty();
        }
    }

    @Nested
    @DisplayName("combiner()")
    class Combiner {

        @Test
        @DisplayName("tous les critères null → aucun filtre, tous les utilisateurs renvoyés")
        void tousCriteresNull_aucunFiltre() {
            Specification<UserJpaEntity> spec = UserSpecifications.combiner(null, null, null);

            assertThat(userJpaRepository.findAll(spec)).hasSize(2);
        }

        @Test
        @DisplayName("combinaison texte + actif + rôle → intersection des trois filtres")
        void combinaisonComplete() {
            Specification<UserJpaEntity> spec = UserSpecifications.combiner("diallo", true, roleAdmin);

            List<UserJpaEntity> resultats = userJpaRepository.findAll(spec);

            assertThat(resultats).extracting(UserJpaEntity::getEmail).containsExactly("awa.diallo@example.com");
        }

        @Test
        @DisplayName("combinaison ne correspondant à aucun utilisateur → résultat vide")
        void combinaisonSansCorrespondance() {
            Specification<UserJpaEntity> spec = UserSpecifications.combiner("diallo", false, null);

            assertThat(userJpaRepository.findAll(spec)).isEmpty();
        }
    }
}
