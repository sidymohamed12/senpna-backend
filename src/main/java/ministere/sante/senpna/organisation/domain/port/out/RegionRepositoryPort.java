package ministere.sante.senpna.organisation.domain.port.out;

import ministere.sante.senpna.organisation.domain.model.Region;
import ministere.sante.senpna.organisation.domain.valueobject.RegionId;

import java.util.List;
import java.util.Optional;

public interface RegionRepositoryPort {

    Optional<Region> findById(RegionId id);

    boolean existsByCode(String code);

    Region save(Region region);

    List<Region> findAll();
}
