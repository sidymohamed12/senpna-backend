package ministere.sante.senpna.shared.infrastructure.persistence;

import ministere.sante.senpna.shared.domain.port.out.RoleQueryPort;
import ministere.sante.senpna.shared.domain.projection.RoleProjection;
import ministere.sante.senpna.shared.infrastructure.exception.NotFoundException;
import ministere.sante.senpna.shared.infrastructure.persistence.entity.RoleJpaEntity;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@Transactional(readOnly = true)
public class RoleQueryAdapter implements RoleQueryPort {

    private final RoleJpaRepository roleJpaRepository;

    public RoleQueryAdapter(RoleJpaRepository roleJpaRepository) {
        this.roleJpaRepository = roleJpaRepository;
    }

    @Override
    public List<RoleProjection> findAll() {
        return roleJpaRepository.findAll()
                .stream()
                .map(r -> new RoleProjection(r.getId(), r.getNom()))
                .toList();
    }

    @Override
    public boolean existsById(UUID roleId) {
        return roleJpaRepository.existsById(roleId);
    }

    @Override
    public String findNomById(UUID roleId) {
        return roleJpaRepository.findNomById(roleId)
                .orElseThrow(() -> new NotFoundException(
                        "Rôle introuvable avec l'identifiant : " + roleId,
                        "ROLE_NOT_FOUND"));
    }

    @Override
    public Optional<RoleJpaEntity> findByCode(String code) {
        return roleJpaRepository.findByCode(code);
    }

    @Override
    public Optional<RoleJpaEntity> findById(UUID id) {
        return roleJpaRepository.findById(id);
    }
}
