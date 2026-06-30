package ministere.sante.senpna.shared.domain.port.out;

import ministere.sante.senpna.shared.domain.projection.RoleProjection;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RoleQueryPort {

    List<RoleProjection> findAll();

    Optional<RoleProjection> findByCode(String code);

    Optional<RoleProjection> findById(UUID id);

    boolean existsById(UUID roleId);

    String findNomById(UUID roleId);
}
