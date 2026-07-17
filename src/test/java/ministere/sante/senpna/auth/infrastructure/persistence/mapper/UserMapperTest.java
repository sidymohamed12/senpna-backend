package ministere.sante.senpna.auth.infrastructure.persistence.mapper;

import ministere.sante.senpna.auth.fixtures.UserFixtures;
import ministere.sante.senpna.auth.infrastructure.persistence.entity.UserJpaEntity;
import ministere.sante.senpna.shared.domain.model.User;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("UserMapper (auth) — conversion domaine ↔ entité JPA")
class UserMapperTest {

    UserMapper sut = new UserMapper();

    @Test
    @DisplayName("toDomain() reporte fidèlement chaque champ, téléphone absent → null")
    void toDomain_telephoneAbsent() {
        UserJpaEntity entity = UserJpaEntity.builder()
            .id(UserFixtures.USER_ID)
            .nom("Diallo")
            .prenom("Mamadou")
            .email(UserFixtures.EMAIL)
            .telephone(null)
            .passwordHash("hash")
            .actif(true)
            .roleIds(Set.of(UserFixtures.ROLE_GESTIONNAIRE_PNA_ID))
            .tentativesEchecConnexion(0)
            .verrouilleJusqua(null)
            .build();

        Instant now = Instant.now();
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);

        User user = sut.toDomain(entity);

        assertThat(user.getId().getValue()).isEqualTo(UserFixtures.USER_ID);
        assertThat(user.getNom().getValue()).isEqualTo("Diallo");
        assertThat(user.getTelephone()).isNull();
        assertThat(user.getRoleIds()).containsExactly(UserFixtures.ROLE_GESTIONNAIRE_PNA_ID);
    }

    @Test
    @DisplayName("toDomain() reporte le téléphone quand présent")
    void toDomain_telephonePresent() {
        UserJpaEntity entity = UserJpaEntity.builder()
            .id(UserFixtures.USER_ID)
            .nom("Diallo")
            .prenom("Mamadou")
            .email(UserFixtures.EMAIL)
            .telephone(UserFixtures.TELEPHONE)
            .passwordHash("hash")
            .actif(true)
            .roleIds(Set.of())
            .tentativesEchecConnexion(0)
            .verrouilleJusqua(null)
            .build();

        Instant now = Instant.now();
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);

        User user = sut.toDomain(entity);

        assertThat(user.getTelephone().value()).isEqualTo(UserFixtures.TELEPHONE);
    }

    @Test
    @DisplayName("toEntity() reporte fidèlement chaque champ de l'agrégat")
    void toEntity_reporteChaqueChamp() {
        User user = UserFixtures.actifAvecTelephone();

        UserJpaEntity entity = sut.toEntity(user);

        assertThat(entity.getId()).isEqualTo(UserFixtures.USER_ID);
        assertThat(entity.getEmail()).isEqualTo(UserFixtures.EMAIL);
        assertThat(entity.getTelephone()).isEqualTo(UserFixtures.TELEPHONE);
        assertThat(entity.isActif()).isTrue();
        assertThat(entity.getCreatedAt()).isEqualTo(user.getCreatedAt());
        assertThat(entity.getUpdatedAt()).isEqualTo(user.getUpdatedAt());

    }

    @Test
    @DisplayName("aller-retour préserve l'état de l'utilisateur")
    void allerRetour_preserveEtat() {
        User original = UserFixtures.multiRoles();

        User restaure = sut.toDomain(sut.toEntity(original));

        assertThat(restaure.getId()).isEqualTo(original.getId());
        assertThat(restaure.getRoleIds()).isEqualTo(original.getRoleIds());
    }
}
