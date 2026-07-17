package ministere.sante.senpna.auth.infrastructure.persistence.mapper;

import ministere.sante.senpna.auth.infrastructure.persistence.entity.UserJpaEntity;
import ministere.sante.senpna.shared.domain.model.User;
import ministere.sante.senpna.shared.domain.valueobject.HashedPassword;
import ministere.sante.senpna.shared.domain.valueobject.Nom;
import ministere.sante.senpna.shared.domain.valueobject.Phone;
import ministere.sante.senpna.shared.domain.valueobject.Prenom;
import ministere.sante.senpna.shared.domain.valueobject.UserId;
import ministere.sante.senpna.shared.domain.valueobject.Email;

import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public User toDomain(UserJpaEntity entity) {
        return User.builder()
            .id(UserId.of(entity.getId()))
            .nom(Nom.of(entity.getNom()))
            .prenom(Prenom.of(entity.getPrenom()))
            .email(Email.of(entity.getEmail()))
            .telephone(entity.getTelephone() != null ? Phone.of(entity.getTelephone()) : null)
            .hashedPassword(HashedPassword.of(entity.getPasswordHash()))
            .actif(entity.isActif())
            .roleIds(entity.getRoleIds())
            .tentativesEchecConnexion(entity.getTentativesEchecConnexion())
            .verrouilleJusqua(entity.getVerrouilleJusqua())
            .createdAt(entity.getCreatedAt())
            .updatedAt(entity.getUpdatedAt())
            .build();
    }

    public UserJpaEntity toEntity(User user) {
        UserJpaEntity entity = UserJpaEntity.builder()
            .id(user.getId().getValue())
            .nom(user.getNom().getValue())
            .prenom(user.getPrenom().getValue())
            .email(user.getEmail().value())
            .telephone(user.getTelephone() != null ? user.getTelephone().value() : null)
            .passwordHash(user.getHashedPassword().value())
            .actif(user.isActif())
            .roleIds(user.getRoleIds())
            .tentativesEchecConnexion(user.getTentativesEchecConnexion())
            .verrouilleJusqua(user.getVerrouilleJusqua())
            .build();
        entity.setCreatedAt(user.getCreatedAt());
        entity.setUpdatedAt(user.getUpdatedAt());
        return entity;
    }
}
