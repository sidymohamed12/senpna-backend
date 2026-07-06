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

    @Column(name = "description", length = 500)
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

    public ProjetJpaEntity(UUID id, CategorieProjet categorie, String nom, String description,
            List<String> objectifs, List<String> impacts, String imageUrl, StatutProjet statut) {
        super(id);
        this.categorie = categorie;
        this.nom = nom;
        this.description = description;
        this.objectifs = objectifs != null ? new ArrayList<>(objectifs) : new ArrayList<>();
        this.impacts = impacts != null ? new ArrayList<>(impacts) : new ArrayList<>();
        this.imageUrl = imageUrl;
        this.statut = statut;
    }
}
