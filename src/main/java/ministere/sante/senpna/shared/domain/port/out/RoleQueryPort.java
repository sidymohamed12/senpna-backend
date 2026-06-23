package ministere.sante.senpna.shared.domain.port.out;

import ministere.sante.senpna.shared.domain.projection.RoleProjection;

import java.util.List;
import java.util.UUID;

public interface RoleQueryPort {

    List<RoleProjection> findAll();

    boolean existsById(UUID roleId);

    String findNomById(UUID roleId);
}
