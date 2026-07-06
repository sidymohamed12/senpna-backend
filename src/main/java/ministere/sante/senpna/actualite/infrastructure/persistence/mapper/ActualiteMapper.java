package ministere.sante.senpna.actualite.infrastructure.persistence.mapper;

import ministere.sante.senpna.actualite.domain.model.Actualite;
import ministere.sante.senpna.actualite.domain.model.ActualiteMedia;
import ministere.sante.senpna.actualite.domain.valueobject.ActualiteId;
import ministere.sante.senpna.actualite.infrastructure.persistence.entity.ActualiteJpaEntity;
import ministere.sante.senpna.actualite.infrastructure.persistence.entity.ActualiteMediaJpaEntity;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class ActualiteMapper {

    public Actualite toDomain(ActualiteJpaEntity entity) {
        List<ActualiteMedia> medias = entity.getMedias().stream()
                .map(m -> ActualiteMedia.reconstruct(m.getId(), m.getType(), m.getUrl(), m.getOrdre()))
                .toList();

        return Actualite.reconstruct(
                ActualiteId.of(entity.getId()),
                entity.getCategorie(),
                entity.getTitre(),
                entity.getDescription(),
                medias,
                entity.getAuteurId(),
                entity.getAuteurNom(),
                List.copyOf(entity.getTags()),
                entity.getStatut(),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }

    /**
     * Construit une nouvelle entité JPA pour un agrégat inédit — cas
     * {@code INSERT}.
     */
    public ActualiteJpaEntity toNewEntity(Actualite actualite) {
        ActualiteJpaEntity entity = new ActualiteJpaEntity(
                actualite.getId().getValue(),
                actualite.getCategorie(),
                actualite.getTitre(),
                actualite.getDescription(),
                actualite.getAuteurId(),
                actualite.getAuteurNom(),
                actualite.getTags(),
                actualite.getStatut());
        entity.setCreatedAt(actualite.getCreatedAt());
        entity.setUpdatedAt(actualite.getUpdatedAt());
        entity.remplacerMedias(mapMedias(actualite));
        return entity;
    }

    /**
     * Met à jour une entité JPA déjà managée (issue de
     * {@code repository.findById}) — préserve la collection Hibernate des
     * médias pour que {@code orphanRemoval} fonctionne correctement.
     */
    public ActualiteJpaEntity updateEntity(ActualiteJpaEntity entity, Actualite actualite) {
        entity.setCategorie(actualite.getCategorie());
        entity.setTitre(actualite.getTitre());
        entity.setDescription(actualite.getDescription());
        entity.setAuteurId(actualite.getAuteurId());
        entity.setAuteurNom(actualite.getAuteurNom());
        entity.setTags(new java.util.ArrayList<>(actualite.getTags()));
        entity.setStatut(actualite.getStatut());
        entity.setUpdatedAt(actualite.getUpdatedAt());
        entity.remplacerMedias(mapMedias(actualite));
        return entity;
    }

    private List<ActualiteMediaJpaEntity> mapMedias(Actualite actualite) {
        return actualite.getMedias().stream()
                .map(this::toMediaEntity)
                .toList();
    }

    private ActualiteMediaJpaEntity toMediaEntity(ActualiteMedia media) {
        UUID id = media.getId();
        return new ActualiteMediaJpaEntity(id, null, media.getType(), media.getUrl(), media.getOrdre());
    }
}
