package ministere.sante.senpna.shared.infrastructure.persistence.adapter;

import ministere.sante.senpna.auth.fixtures.UserFixtures;
import ministere.sante.senpna.auth.infrastructure.persistence.mapper.UserMapper;
import ministere.sante.senpna.auth.infrastructure.persistence.repository.UserJpaRepository;
import ministere.sante.senpna.shared.domain.projection.UserAffectationView;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Import({ UserAffectationRepositoryAdapter.class, UserMapper.class })
@DisplayName("UserAffectationRepositoryAdapter — affectation organisationnelle avec H2")
class UserAffectationRepositoryAdapterTest {

    @Autowired
    UserJpaRepository userJpaRepository;
    @Autowired
    UserMapper userMapper;
    @Autowired
    UserAffectationRepositoryAdapter sut;
    @Autowired
    TestEntityManager entityManager;

    @BeforeEach
    void setUp() {
        userJpaRepository.save(userMapper.toEntity(UserFixtures.actif()));
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @DisplayName("existsUtilisateur() reflète l'existence en base")
    void existsUtilisateur() {
        assertThat(sut.existsUtilisateur(UserFixtures.USER_ID)).isTrue();
        assertThat(sut.existsUtilisateur(UUID.randomUUID())).isFalse();
    }

    @Test
    @DisplayName("findAffectation() sur un utilisateur sans affectation → vue avec champs null")
    void findAffectation_sansAffectation() {
        Optional<UserAffectationView> result = sut.findAffectation(UserFixtures.USER_ID);

        assertThat(result).isPresent();
        assertThat(result.get().entrepotId()).isNull();
        assertThat(result.get().structureSanitaireId()).isNull();
        assertThat(result.get().fournisseurId()).isNull();
    }

    @Test
    @DisplayName("affecterEntrepot() affecte l'entrepôt et efface toute structure sanitaire ou fournisseur précédent")
    void affecterEntrepot_affecteEtEcraseStructure() {
        UUID entrepotId = UUID.randomUUID();

        sut.affecterEntrepot(UserFixtures.USER_ID, entrepotId);
        entityManager.flush();
        entityManager.clear();

        UserAffectationView result = sut.findAffectation(UserFixtures.USER_ID).orElseThrow();
        assertThat(result.entrepotId()).isEqualTo(entrepotId);
        assertThat(result.structureSanitaireId()).isNull();
        assertThat(result.fournisseurId()).isNull();
    }

    @Test
    @DisplayName("affecterStructureSanitaire() affecte la structure et efface tout entrepôt ou fournisseur précédent")
    void affecterStructureSanitaire_affecteEtEcraseEntrepot() {
        sut.affecterEntrepot(UserFixtures.USER_ID, UUID.randomUUID());
        entityManager.flush();
        entityManager.clear();

        UUID structureId = UUID.randomUUID();
        sut.affecterStructureSanitaire(UserFixtures.USER_ID, structureId);
        entityManager.flush();
        entityManager.clear();

        UserAffectationView result = sut.findAffectation(UserFixtures.USER_ID).orElseThrow();
        assertThat(result.structureSanitaireId()).isEqualTo(structureId);
        assertThat(result.entrepotId()).isNull();
        assertThat(result.fournisseurId()).isNull();
    }

    @Test
    @DisplayName("affecterFournisseur() affecte le fournisseur et efface tout entrepôt ou structure précédent")
    void affecterFournisseur_affecteEtEcraseEntrepotEtStructure() {
        sut.affecterEntrepot(UserFixtures.USER_ID, UUID.randomUUID());
        entityManager.flush();
        entityManager.clear();

        UUID fournisseurId = UUID.randomUUID();
        sut.affecterFournisseur(UserFixtures.USER_ID, fournisseurId);
        entityManager.flush();
        entityManager.clear();

        UserAffectationView result = sut.findAffectation(UserFixtures.USER_ID).orElseThrow();
        assertThat(result.fournisseurId()).isEqualTo(fournisseurId);
        assertThat(result.entrepotId()).isNull();
        assertThat(result.structureSanitaireId()).isNull();
    }

    @Test
    @DisplayName("retirerAffectation() efface entrepôt, structure sanitaire ET fournisseur")
    void retirerAffectation_efaceLesTrois() {
        sut.affecterFournisseur(UserFixtures.USER_ID, UUID.randomUUID());
        entityManager.flush();
        entityManager.clear();

        sut.retirerAffectation(UserFixtures.USER_ID);
        entityManager.flush();
        entityManager.clear();

        UserAffectationView result = sut.findAffectation(UserFixtures.USER_ID).orElseThrow();
        assertThat(result.entrepotId()).isNull();
        assertThat(result.structureSanitaireId()).isNull();
        assertThat(result.fournisseurId()).isNull();
    }

    @Test
    @DisplayName("findAffectation() sur un utilisateur inexistant → Optional vide")
    void findAffectation_utilisateurInexistant_vide() {
        assertThat(sut.findAffectation(UUID.randomUUID())).isEmpty();
    }
}
