package ministere.sante.senpna.organisation.infrastructure.persistence.adapter;

import ministere.sante.senpna.organisation.domain.model.Region;
import ministere.sante.senpna.organisation.domain.port.out.RegionRepositoryPort;
import ministere.sante.senpna.organisation.domain.valueobject.RegionId;
import ministere.sante.senpna.organisation.infrastructure.persistence.entity.RegionJpaEntity;
import ministere.sante.senpna.organisation.infrastructure.persistence.mapper.RegionMapper;
import ministere.sante.senpna.organisation.infrastructure.persistence.repository.RegionJpaRepository;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Component
public class RegionRepositoryAdapter implements RegionRepositoryPort {

    private final RegionJpaRepository regionJpaRepository;
    private final RegionMapper regionMapper;

    public RegionRepositoryAdapter(RegionJpaRepository regionJpaRepository, RegionMapper regionMapper) {
        this.regionJpaRepository = regionJpaRepository;
        this.regionMapper = regionMapper;
    }

    @Override
    public Optional<Region> findById(RegionId id) {
        return regionJpaRepository.findById(id.getValue()).map(regionMapper::toDomain);
    }

    @Override
    public boolean existsByCode(String code) {
        return regionJpaRepository.existsByCode(code);
    }

    @Override
    @Transactional
    public Region save(Region region) {
        RegionJpaEntity saved = regionJpaRepository.save(regionMapper.toEntity(region));
        return regionMapper.toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Region> findAll() {
        return regionJpaRepository.findAll(Sort.by(Sort.Direction.ASC, "nom")).stream()
                .map(regionMapper::toDomain)
                .toList();
    }
}
