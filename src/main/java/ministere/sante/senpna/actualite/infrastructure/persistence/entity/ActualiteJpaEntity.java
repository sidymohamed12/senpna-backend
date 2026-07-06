package ministere.sante.senpna.actualite.infrastructure.persistence.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import ministere.sante.senpna.actualite.domain.valueobject.CategorieActualite;
import ministere.sante.senpna.actualite.domain.valueobject.StatutActualite;
import ministere.sante.senpna.shared.infrastructure.persistence.entity.BaseJpaEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "actualites")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(callSuper = false)
public class ActualiteJpaEntity extends BaseJpaEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "categorie", nullable = false, length = 30)
    private CategorieActualite categorie;

    @Column(name = "titre", nullable = false, length = 200)
    private String titre;

    @Column(name = "description", length = 700)
    private String description;

    @OneToMany(mappedBy = "actualite", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("ordre ASC")
    @EqualsAndHashCode.Exclude
    private List<ActualiteMediaJpaEntity> medias = new ArrayList<>();

    @Column(name = "auteur_id", nullable = false)
    private UUID auteurId;

    @Column(name = "auteur_nom", nullable = false, length = 200)
    private String auteurNom;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "actualite_tags", joinColumns = @JoinColumn(name = "actualite_id"))
    @Column(name = "tag", length = 50)
    @OrderBy
    @EqualsAndHashCode.Exclude
    private List<String> tags = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false, length = 20)
    private StatutActualite statut;

    public ActualiteJpaEntity(UUID id, CategorieActualite categorie, String titre, String description,
            UUID auteurId, String auteurNom, List<String> tags, StatutActualite statut) {
        super(id);
        this.categorie = categorie;
        this.titre = titre;
        this.description = description;
        this.auteurId = auteurId;
        this.auteurNom = auteurNom;
        this.tags = tags != null ? new ArrayList<>(tags) : new ArrayList<>();
        this.statut = statut;
    }

    /**
     * Remplace intégralement les médias — {@code orphanRemoval = true}
     * supprime en base les médias qui ne sont plus référencés.
     */
    public void remplacerMedias(List<ActualiteMediaJpaEntity> nouveauxMedias) {
        this.medias.clear();
        if (nouveauxMedias != null) {
            for (ActualiteMediaJpaEntity media : nouveauxMedias) {
                media.setActualite(this);
                this.medias.add(media);
            }
        }
    }
}
