package ministere.sante.senpna.shared.domain.port.out;

import ministere.sante.senpna.shared.domain.projection.RoleProjection;
import ministere.sante.senpna.shared.infrastructure.persistence.entity.RoleJpaEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RoleQueryPort {

    List<RoleProjection> findAll();

    Optional<RoleJpaEntity> findByCode(String code);

    Optional<RoleJpaEntity> findById(UUID id);

    boolean existsById(UUID roleId);

    String findNomById(UUID roleId);
}
