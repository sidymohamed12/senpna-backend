package ministere.sante.senpna.projet.infrastructure.persistence.mapper;

import ministere.sante.senpna.projet.domain.model.Projet;
import ministere.sante.senpna.projet.domain.valueobject.ProjetId;
import ministere.sante.senpna.projet.infrastructure.persistence.entity.ProjetJpaEntity;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ProjetMapper {

    public Projet toDomain(ProjetJpaEntity entity) {
        return Projet.reconstruct(
                ProjetId.of(entity.getId()),
                entity.getCategorie(),
                entity.getNom(),
                entity.getDescription(),
                List.copyOf(entity.getObjectifs()),
                List.copyOf(entity.getImpacts()),
                entity.getImageUrl(),
                entity.getStatut(),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }

    public ProjetJpaEntity toEntity(Projet projet) {
        ProjetJpaEntity entity = new ProjetJpaEntity(
                projet.getId().getValue(),
                projet.getCategorie(),
                projet.getNom(),
                projet.getDescription(),
                projet.getObjectifs(),
                projet.getImpacts(),
                projet.getImageUrl(),
                projet.getStatut());
        entity.setCreatedAt(projet.getCreatedAt());
        entity.setUpdatedAt(projet.getUpdatedAt());
        return entity;
    }
}
