package ministere.sante.senpna.fournisseur.infrastructure.persistence.mapper;

import ministere.sante.senpna.fournisseur.domain.model.Fournisseur;
import ministere.sante.senpna.fournisseur.domain.valueobject.FournisseurId;
import ministere.sante.senpna.fournisseur.infrastructure.persistence.entity.FournisseurJpaEntity;

import org.springframework.stereotype.Component;

@Component
public class FournisseurMapper {

    public Fournisseur toDomain(FournisseurJpaEntity entity) {
        return Fournisseur.reconstruct(
                FournisseurId.of(entity.getId()),
                entity.getNom(),
                entity.getAdresse(),
                entity.getTelephone(),
                entity.getEmail(),
                entity.getContactPrincipal(),
                entity.isActif(),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }

    public FournisseurJpaEntity toEntity(Fournisseur fournisseur) {
        FournisseurJpaEntity entity = new FournisseurJpaEntity(
                fournisseur.getId().getValue(),
                fournisseur.getNom(),
                fournisseur.getAdresse(),
                fournisseur.getTelephone(),
                fournisseur.getEmail(),
                fournisseur.getContactPrincipal(),
                fournisseur.isActif());
        entity.setCreatedAt(fournisseur.getCreatedAt());
        entity.setUpdatedAt(fournisseur.getUpdatedAt());
        return entity;
    }
}
