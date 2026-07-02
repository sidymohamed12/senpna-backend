package ministere.sante.senpna.organisation.infrastructure.persistence.repository;

import ministere.sante.senpna.organisation.infrastructure.persistence.entity.UserAffectationJpaEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface UserAffectationJpaRepository extends JpaRepository<UserAffectationJpaEntity, UUID> {

        @Modifying
        @Query("UPDATE UserAffectationJpaEntity u SET u.entrepotId = :entrepotId, u.structureSanitaireId = null "
                        + "WHERE u.id = :userId")
        int affecterEntrepot(@Param("userId") UUID userId, @Param("entrepotId") UUID entrepotId);

        @Modifying
        @Query("UPDATE UserAffectationJpaEntity u SET u.structureSanitaireId = :structureSanitaireId, "
                        + "u.entrepotId = null WHERE u.id = :userId")
        int affecterStructureSanitaire(@Param("userId") UUID userId,
                        @Param("structureSanitaireId") UUID structureSanitaireId);

        @Modifying
        @Query("UPDATE UserAffectationJpaEntity u SET u.entrepotId = null, u.structureSanitaireId = null "
                        + "WHERE u.id = :userId")
        int retirerAffectation(@Param("userId") UUID userId);
}
