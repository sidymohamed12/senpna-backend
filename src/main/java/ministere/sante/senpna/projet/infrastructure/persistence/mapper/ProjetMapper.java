package ministere.sante.senpna.projet.infrastructure.persistence.mapper;

import ministere.sante.senpna.projet.domain.model.Projet;
import ministere.sante.senpna.projet.domain.valueobject.ProjetId;
import ministere.sante.senpna.projet.infrastructure.persistence.entity.ProjetJpaEntity;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ProjetMapper {

    public Projet toDomain(ProjetJpaEntity entity) {
        return Projet.builder()
            .id(ProjetId.of(entity.getId()))
            .categorie(entity.getCategorie())
            .nom(entity.getNom())
            .description(entity.getDescription())
            .objectifs(List.copyOf(entity.getObjectifs()))
            .impacts(List.copyOf(entity.getImpacts()))
            .imageUrl(entity.getImageUrl())
            .statut(entity.getStatut())
            .createdAt(entity.getCreatedAt())
            .updatedAt(entity.getUpdatedAt())
            .build();
    }

    public ProjetJpaEntity toEntity(Projet projet) {
        ProjetJpaEntity entity = ProjetJpaEntity.builder()
            .id(projet.getId().getValue())
            .categorie(projet.getCategorie())
            .nom(projet.getNom())
            .description(projet.getDescription())
            .objectifs(projet.getObjectifs())
            .impacts(projet.getImpacts())
            .imageUrl(projet.getImageUrl())
            .statut(projet.getStatut())
            .build();
        entity.setCreatedAt(projet.getCreatedAt());
        entity.setUpdatedAt(projet.getUpdatedAt());
        return entity;
    }
}
