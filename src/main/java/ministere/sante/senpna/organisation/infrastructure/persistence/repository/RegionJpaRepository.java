package ministere.sante.senpna.organisation.infrastructure.persistence.repository;

import ministere.sante.senpna.organisation.infrastructure.persistence.entity.RegionJpaEntity;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface RegionJpaRepository extends JpaRepository<RegionJpaEntity, UUID> {

    boolean existsByCode(String code);
}
