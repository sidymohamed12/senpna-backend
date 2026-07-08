package ministere.sante.senpna.carriere.infrastructure.persistence.mapper;

import ministere.sante.senpna.carriere.domain.model.Candidature;
import ministere.sante.senpna.carriere.domain.valueobject.CandidatureId;
import ministere.sante.senpna.carriere.infrastructure.persistence.entity.CandidatureJpaEntity;
import ministere.sante.senpna.shared.domain.valueobject.Email;
import ministere.sante.senpna.shared.domain.valueobject.Phone;

import org.springframework.stereotype.Component;

@Component
public class CandidatureMapper {

    public Candidature toDomain(CandidatureJpaEntity entity) {
        return Candidature.reconstruct(
                CandidatureId.of(entity.getId()),
                entity.getOpportuniteId(),
                entity.getCivilite(),
                entity.getNomComplet(),
                Email.of(entity.getEmail()),
                Phone.of(entity.getTelephone()),
                entity.getCvUrl(),
                entity.getLettreMotivationUrl(),
                entity.getMessageComplementaire(),
                entity.isConsentementRgpd(),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }

    /**
     * Une candidature n'est jamais modifiée après soumission — pas de
     * {@code updateEntity}, uniquement une création.
     */
    public CandidatureJpaEntity toNewEntity(Candidature candidature) {
        CandidatureJpaEntity entity = new CandidatureJpaEntity(
                candidature.getId().getValue(),
                candidature.getOpportuniteId(),
                candidature.getCivilite(),
                candidature.getNomComplet(),
                candidature.getEmail().value(),
                candidature.getTelephone().value(),
                candidature.getCvUrl(),
                candidature.getLettreMotivationUrl(),
                candidature.getMessageComplementaire(),
                candidature.isConsentementRgpd());
        entity.setCreatedAt(candidature.getCreatedAt());
        entity.setUpdatedAt(candidature.getUpdatedAt());
        return entity;
    }
}
