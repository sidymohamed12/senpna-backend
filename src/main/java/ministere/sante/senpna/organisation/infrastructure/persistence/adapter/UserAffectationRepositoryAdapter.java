package ministere.sante.senpna.organisation.infrastructure.persistence.adapter;

import ministere.sante.senpna.organisation.domain.port.out.UserAffectationRepositoryPort;
import ministere.sante.senpna.organisation.domain.port.out.UserAffectationView;
import ministere.sante.senpna.organisation.infrastructure.persistence.entity.UserAffectationJpaEntity;
import ministere.sante.senpna.organisation.infrastructure.persistence.repository.UserAffectationJpaRepository;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Component
public class UserAffectationRepositoryAdapter implements UserAffectationRepositoryPort {

    private final UserAffectationJpaRepository userAffectationJpaRepository;

    public UserAffectationRepositoryAdapter(UserAffectationJpaRepository userAffectationJpaRepository) {
        this.userAffectationJpaRepository = userAffectationJpaRepository;
    }

    @Override
    public boolean existsUtilisateur(UUID userId) {
        return userAffectationJpaRepository.existsById(userId);
    }

    @Override
    @Transactional
    public void affecterEntrepot(UUID userId, UUID entrepotId) {
        userAffectationJpaRepository.affecterEntrepot(userId, entrepotId);
    }

    @Override
    @Transactional
    public void affecterStructureSanitaire(UUID userId, UUID structureSanitaireId) {
        userAffectationJpaRepository.affecterStructureSanitaire(userId, structureSanitaireId);
    }

    @Override
    @Transactional
    public void retirerAffectation(UUID userId) {
        userAffectationJpaRepository.retirerAffectation(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserAffectationView> findAffectation(UUID userId) {
        return userAffectationJpaRepository.findById(userId)
                .map(this::toView);
    }

    private UserAffectationView toView(UserAffectationJpaEntity entity) {
        return new UserAffectationView(entity.getId(), entity.getEntrepotId(), entity.getStructureSanitaireId());
    }
}
