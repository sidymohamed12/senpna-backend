package ministere.sante.senpna.fournisseur.infrastructure.persistence.mapper;

import ministere.sante.senpna.fournisseur.domain.model.Fournisseur;
import ministere.sante.senpna.fournisseur.domain.valueobject.FournisseurId;
import ministere.sante.senpna.fournisseur.infrastructure.persistence.entity.FournisseurJpaEntity;

import org.springframework.stereotype.Component;

@Component
public class FournisseurMapper {

    public Fournisseur toDomain(FournisseurJpaEntity entity) {
        return Fournisseur.builder()
            .id(FournisseurId.of(entity.getId()))
            .nom(entity.getNom())
            .adresse(entity.getAdresse())
            .telephone(entity.getTelephone())
            .email(entity.getEmail())
            .contactPrincipal(entity.getContactPrincipal())
            .actif(entity.isActif())
            .createdAt(entity.getCreatedAt())
            .updatedAt(entity.getUpdatedAt())
            .build();
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
