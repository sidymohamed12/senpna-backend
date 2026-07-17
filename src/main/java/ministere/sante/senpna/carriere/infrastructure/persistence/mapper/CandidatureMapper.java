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
        return Candidature.builder()
            .id(CandidatureId.of(entity.getId()))
            .opportuniteId(entity.getOpportuniteId())
            .civilite(entity.getCivilite())
            .nomComplet(entity.getNomComplet())
            .email(Email.of(entity.getEmail()))
            .telephone(Phone.of(entity.getTelephone()))
            .cvUrl(entity.getCvUrl())
            .lettreMotivationUrl(entity.getLettreMotivationUrl())
            .messageComplementaire(entity.getMessageComplementaire())
            .consentementRgpd(entity.isConsentementRgpd())
            .createdAt(entity.getCreatedAt())
            .updatedAt(entity.getUpdatedAt())
            .build();
    }

    /**
     * Une candidature n'est jamais modifiée après soumission — pas de
     * {@code updateEntity}, uniquement une création.
     */
    public CandidatureJpaEntity toNewEntity(Candidature candidature) {
        CandidatureJpaEntity entity = CandidatureJpaEntity.builder()
            .id(candidature.getId().getValue())
            .opportuniteId(candidature.getOpportuniteId())
            .civilite(candidature.getCivilite())
            .nomComplet(candidature.getNomComplet())
            .email(candidature.getEmail().value())
            .telephone(candidature.getTelephone().value())
            .cvUrl(candidature.getCvUrl())
            .lettreMotivationUrl(candidature.getLettreMotivationUrl())
            .messageComplementaire(candidature.getMessageComplementaire())
            .consentementRgpd(candidature.isConsentementRgpd())
            .build();
        entity.setCreatedAt(candidature.getCreatedAt());
        entity.setUpdatedAt(candidature.getUpdatedAt());
        return entity;
    }
}
