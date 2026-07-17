package ministere.sante.senpna.projet.domain.model;

import ministere.sante.senpna.projet.domain.valueobject.CategorieProjet;
import ministere.sante.senpna.projet.domain.valueobject.ProjetId;
import ministere.sante.senpna.projet.domain.valueobject.StatutProjet;
import ministere.sante.senpna.shared.domain.model.AggregateRoot;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Projet extends AggregateRoot<ProjetId> {

    private static final int NOM_MAX_LENGTH = 200;
    private static final int DESCRIPTION_MAX_LENGTH = 700;
    private static final int IMAGE_URL_MAX_LENGTH = 1000;
    private static final int LISTE_VALEURS_MAX = 10;
    private static final int VALEUR_MAX_LENGTH = 400;

    private CategorieProjet categorie;
    private String nom;
    private String description;
    private List<String> objectifs;
    private List<String> impacts;
    private String imageUrl;
    private StatutProjet statut;

    private Projet(Builder builder) {
        super(builder.id, builder.createdAt, builder.updatedAt);
        this.categorie = validerCategorie(builder.categorie);
        this.nom = validerNom(builder.nom);
        this.description = validerDescription(builder.description);
        this.objectifs = validerListeValeurs(builder.objectifs, "objectif");
        this.impacts = validerListeValeurs(builder.impacts, "impact");
        this.imageUrl = validerImageUrl(builder.imageUrl);
        this.statut = builder.statut != null ? builder.statut : StatutProjet.BROUILLON;
    }

    /** Données nécessaires à la création d'un nouveau projet. */
    public record CreationCommand(CategorieProjet categorie, String nom, String description,
            List<String> objectifs, List<String> impacts, String imageUrl) {
    }

    public static Projet creer(CreationCommand command) {
        Instant maintenant = Instant.now();
        return builder()
                .id(ProjetId.generate())
                .categorie(command.categorie())
                .nom(command.nom())
                .description(command.description())
                .objectifs(command.objectifs())
                .impacts(command.impacts())
                .imageUrl(command.imageUrl())
                .statut(StatutProjet.BROUILLON)
                .createdAt(maintenant)
                .updatedAt(maintenant)
                .build();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private ProjetId id;
        private CategorieProjet categorie;
        private String nom;
        private String description;
        private List<String> objectifs;
        private List<String> impacts;
        private String imageUrl;
        private StatutProjet statut;
        private Instant createdAt;
        private Instant updatedAt;

        private Builder() {
        }

        public Builder id(ProjetId id) {
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

        public Builder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder updatedAt(Instant updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public Projet build() {
            return new Projet(this);
        }
    }

    // ── Comportements métier ────────────────────────────────────────────

    public void modifierContenu(CategorieProjet categorie, String nom, String description, List<String> objectifs,
            List<String> impacts, String imageUrl) {
        this.categorie = validerCategorie(categorie);
        this.nom = validerNom(nom);
        this.description = validerDescription(description);
        this.objectifs = validerListeValeurs(objectifs, "objectif");
        this.impacts = validerListeValeurs(impacts, "impact");
        this.imageUrl = validerImageUrl(imageUrl);
        markUpdated();
    }

    public void publier() {
        if (this.statut == StatutProjet.PUBLIE) {
            return;
        }
        this.statut = StatutProjet.PUBLIE;
        markUpdated();
    }

    public void archiver() {
        if (this.statut == StatutProjet.ARCHIVE) {
            return;
        }
        this.statut = StatutProjet.ARCHIVE;
        markUpdated();
    }

    public void desactiver() {
        if (this.statut == StatutProjet.DESACTIVE) {
            return;
        }
        this.statut = StatutProjet.DESACTIVE;
        markUpdated();
    }

    public void remettreEnBrouillon() {
        if (this.statut == StatutProjet.BROUILLON) {
            return;
        }
        this.statut = StatutProjet.BROUILLON;
        markUpdated();
    }

    // ── Validation ───────────────────────────────────────────────────────

    private static CategorieProjet validerCategorie(CategorieProjet categorie) {
        return Objects.requireNonNull(categorie, "La catégorie du projet est obligatoire");
    }

    private static String validerNom(String nom) {
        Objects.requireNonNull(nom, "Le nom du projet ne peut pas être null");
        String trimmed = nom.trim();
        if (trimmed.isBlank()) {
            throw new IllegalArgumentException("Le nom du projet ne peut pas être vide");
        }
        if (trimmed.length() > NOM_MAX_LENGTH) {
            throw new IllegalArgumentException(
                    "Le nom du projet ne peut pas dépasser " + NOM_MAX_LENGTH + " caractères");
        }
        return trimmed;
    }

    private static String validerDescription(String description) {
        if (description == null) {
            return null;
        }
        String trimmed = description.trim();
        if (trimmed.length() > DESCRIPTION_MAX_LENGTH) {
            throw new IllegalArgumentException(
                    "La description ne peut pas dépasser " + DESCRIPTION_MAX_LENGTH + " caractères");
        }
        return trimmed;
    }

    private static String validerImageUrl(String imageUrl) {
        if (imageUrl == null) {
            return null;
        }
        String trimmed = imageUrl.trim();
        if (trimmed.isBlank()) {
            return null;
        }
        if (trimmed.length() > IMAGE_URL_MAX_LENGTH) {
            throw new IllegalArgumentException(
                    "L'URL de l'image ne peut pas dépasser " + IMAGE_URL_MAX_LENGTH + " caractères");
        }
        return trimmed;
    }

    private static List<String> validerListeValeurs(List<String> valeurs, String libelleChamp) {
        if (valeurs == null || valeurs.isEmpty()) {
            return Collections.emptyList();
        }
        if (valeurs.size() > LISTE_VALEURS_MAX) {
            throw new IllegalArgumentException(
                    "Un projet ne peut pas avoir plus de " + LISTE_VALEURS_MAX + " " + libelleChamp + "s");
        }
        List<String> nettoyees = new ArrayList<>();
        for (String valeur : valeurs) {
            if (valeur == null || valeur.isBlank()) {
                continue;
            }
            String trimmed = valeur.trim();
            if (trimmed.length() > VALEUR_MAX_LENGTH) {
                throw new IllegalArgumentException(
                        "Un " + libelleChamp + " ne peut pas dépasser " + VALEUR_MAX_LENGTH + " caractères");
            }
            nettoyees.add(trimmed);
        }
        return Collections.unmodifiableList(nettoyees);
    }

    // ── Accesseurs ───────────────────────────────────────────────────────

    public CategorieProjet getCategorie() {
        return categorie;
    }

    public String getNom() {
        return nom;
    }

    public String getDescription() {
        return description;
    }

    public List<String> getObjectifs() {
        return Collections.unmodifiableList(objectifs);
    }

    public List<String> getImpacts() {
        return Collections.unmodifiableList(impacts);
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public StatutProjet getStatut() {
        return statut;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }
}
