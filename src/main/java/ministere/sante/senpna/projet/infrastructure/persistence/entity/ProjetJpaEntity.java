package ministere.sante.senpna.projet.infrastructure.persistence.entity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import ministere.sante.senpna.projet.domain.valueobject.CategorieProjet;
import ministere.sante.senpna.projet.domain.valueobject.StatutProjet;
import ministere.sante.senpna.shared.infrastructure.persistence.entity.BaseJpaEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "projets")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(callSuper = false)
public class ProjetJpaEntity extends BaseJpaEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "categorie", nullable = false, length = 30)
    private CategorieProjet categorie;

    @Column(name = "nom", nullable = false, length = 200)
    private String nom;

    @Column(name = "description", length = 700)
    private String description;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "projet_objectifs", joinColumns = @JoinColumn(name = "projet_id"))
    @Column(name = "objectif", length = 400)
    @OrderColumn(name = "ordre")
    @EqualsAndHashCode.Exclude
    private List<String> objectifs = new ArrayList<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "projet_impacts", joinColumns = @JoinColumn(name = "projet_id"))
    @Column(name = "impact", length = 400)
    @OrderColumn(name = "ordre")
    @EqualsAndHashCode.Exclude
    private List<String> impacts = new ArrayList<>();

    @Column(name = "image_url", length = 1000)
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false, length = 20)
    private StatutProjet statut;

    private ProjetJpaEntity(Builder builder) {
        super(builder.id);
        this.categorie = builder.categorie;
        this.nom = builder.nom;
        this.description = builder.description;
        this.objectifs = builder.objectifs != null ? new ArrayList<>(builder.objectifs) : new ArrayList<>();
        this.impacts = builder.impacts != null ? new ArrayList<>(builder.impacts) : new ArrayList<>();
        this.imageUrl = builder.imageUrl;
        this.statut = builder.statut;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private UUID id;
        private CategorieProjet categorie;
        private String nom;
        private String description;
        private List<String> objectifs;
        private List<String> impacts;
        private String imageUrl;
        private StatutProjet statut;

        private Builder() {
        }

        public Builder id(UUID id) {
            this.id = id;
            return this;
        }

        public Builder categorie(CategorieProjet categorie) {
            this.categorie = categorie;
            return this;
        }

        public Builder nom(String nom) {
            this.nom = nom;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder objectifs(List<String> objectifs) {
            this.objectifs = objectifs;
            return this;
        }

        public Builder impacts(List<String> impacts) {
            this.impacts = impacts;
            return this;
        }

        public Builder imageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
            return this;
        }

        public Builder statut(StatutProjet statut) {
            this.statut = statut;
            return this;
        }

        public ProjetJpaEntity build() {
            return new ProjetJpaEntity(this);
        }
    }
}
