package ministere.sante.senpna.auth.infrastructure.persistence.adapter;

import ministere.sante.senpna.auth.infrastructure.persistence.entity.RoleJpaEntity;
import ministere.sante.senpna.auth.infrastructure.persistence.repository.RoleJpaRepository;
import ministere.sante.senpna.shared.domain.port.out.RoleQueryPort;
import ministere.sante.senpna.shared.domain.projection.RoleProjection;
import ministere.sante.senpna.shared.domain.exception.ErrorCategory;
import ministere.sante.senpna.shared.domain.exception.SenPnaException;

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
                .map(this::toProjection)
                .toList();
    }

    @Override
    public boolean existsById(UUID roleId) {
        return roleJpaRepository.existsById(roleId);
    }

    @Override
    public String findNomById(UUID roleId) {
        return roleJpaRepository.findNomById(roleId)
                .orElseThrow(() -> new SenPnaException(
                        "Rôle introuvable avec l'identifiant : " + roleId,
                        "ROLE_NOT_FOUND", ErrorCategory.NOT_FOUND));
    }

    @Override
    public Optional<RoleProjection> findByCode(String code) {
        return roleJpaRepository.findByCode(code).map(this::toProjection);
    }

    @Override
    public Optional<RoleProjection> findById(UUID id) {
        return roleJpaRepository.findById(id).map(this::toProjection);
    }

    private RoleProjection toProjection(RoleJpaEntity entity) {
        return new RoleProjection(entity.getId(), entity.getCode(), entity.getNom());
    }
}
