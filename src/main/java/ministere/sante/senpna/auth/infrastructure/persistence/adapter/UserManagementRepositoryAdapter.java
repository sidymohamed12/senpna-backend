package ministere.sante.senpna.auth.infrastructure.persistence.adapter;

import org.springframework.data.jpa.domain.Specification;

import ministere.sante.senpna.shared.domain.port.out.UserManagementRepositoryPort;
import ministere.sante.senpna.shared.domain.valueobject.PageResult;
import ministere.sante.senpna.auth.infrastructure.persistence.entity.UserJpaEntity;
import ministere.sante.senpna.auth.infrastructure.persistence.mapper.UserMapper;
import ministere.sante.senpna.auth.infrastructure.persistence.repository.UserJpaRepository;
import ministere.sante.senpna.auth.infrastructure.persistence.specification.UserSpecifications;
import ministere.sante.senpna.shared.domain.criteria.UserSearchCriteria;
import ministere.sante.senpna.shared.domain.model.User;
import ministere.sante.senpna.shared.domain.valueobject.UserId;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import ministere.sante.senpna.shared.domain.valueobject.Email;
import ministere.sante.senpna.shared.domain.valueobject.PageRequest;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Component
public class UserManagementRepositoryAdapter implements UserManagementRepositoryPort {

    private final UserJpaRepository userJpaRepository;
    private final UserMapper userMapper;

    /**
     * Champs de tri autorisés — liste blanche : évite qu'un paramètre de
     * requête arbitraire ne soit injecté tel quel dans le tri JPA.
     */
    private static final Set<String> CHAMPS_TRI_AUTORISES = Set.of(
            "nom", "prenom", "email", "actif", "createdAt", "updatedAt");

    public UserManagementRepositoryAdapter(UserJpaRepository userJpaRepository, UserMapper userMapper) {
        this.userJpaRepository = userJpaRepository;
        this.userMapper = userMapper;
    }

    @Override
    public Optional<User> findById(UserId id) {
        return userJpaRepository.findById(id.getValue()).map(userMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<User> search(UserSearchCriteria criteria, PageRequest pageRequest) {
        Specification<UserJpaEntity> specification = UserSpecifications.combiner(
                criteria.recherche(), criteria.actif(), criteria.roleId());

        Sort.Direction direction = pageRequest.direction() == PageRequest.SortDirection.ASC
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;
        String champTri = CHAMPS_TRI_AUTORISES.contains(pageRequest.sortBy())
                ? pageRequest.sortBy()
                : "createdAt";

        Pageable pageable = org.springframework.data.domain.PageRequest.of(
                pageRequest.page(), pageRequest.size(), Sort.by(direction, champTri));

        Page<UserJpaEntity> page = userJpaRepository.findAll(specification, pageable);

        List<User> content = page.getContent().stream().map(userMapper::toDomain).toList();

        return PageResult.of(content, page.getNumber(), page.getSize(), page.getTotalElements());
    }

    @Override
    @Transactional
    public User save(User user) {
        UserJpaEntity saved = userJpaRepository.save(userMapper.toEntity(user));
        return userMapper.toDomain(saved);
    }

    @Override
    public boolean existsByEmail(Email email) {
        return userJpaRepository.existsByEmail(email.value());
    }

}
