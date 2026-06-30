package ministere.sante.senpna.shared.infrastructure.persistence;

import ministere.sante.senpna.shared.infrastructure.persistence.entity.RoleJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface RoleJpaRepository extends JpaRepository<RoleJpaEntity, UUID> {

    Optional<RoleJpaEntity> findByCode(String code);

    Optional<RoleJpaEntity> findById(UUID id);

    boolean existsById(UUID id);

    @Query("SELECT r.nom FROM RoleJpaEntity r WHERE r.id = :id")
    Optional<String> findNomById(UUID id);
}
