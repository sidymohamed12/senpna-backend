package ministere.sante.senpna.actualite.domain.model;

import ministere.sante.senpna.actualite.domain.valueobject.ActualiteId;
import ministere.sante.senpna.actualite.domain.valueobject.CategorieActualite;
import ministere.sante.senpna.actualite.domain.valueobject.StatutActualite;
import ministere.sante.senpna.shared.domain.model.AggregateRoot;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class Actualite extends AggregateRoot<ActualiteId> {

    private static final int TITRE_MAX_LENGTH = 200;
    private static final int DESCRIPTION_MAX_LENGTH = 500;
    private static final int AUTEUR_NOM_MAX_LENGTH = 200;
    private static final int MEDIAS_MAX = 10;
    private static final int TAGS_MAX = 20;
    private static final int TAG_MAX_LENGTH = 50;

    private CategorieActualite categorie;
    private String titre;
    private String description;
    private List<ActualiteMedia> medias;
    private final UUID auteurId;
    private String auteurNom;
    private List<String> tags;
    private StatutActualite statut;

    private Actualite(ActualiteId id, CategorieActualite categorie, String titre, String description,
            List<ActualiteMedia> medias, UUID auteurId, String auteurNom, List<String> tags, StatutActualite statut,
            Instant createdAt, Instant updatedAt) {
        super(id, createdAt, updatedAt);
        this.categorie = validerCategorie(categorie);
        this.titre = validerTitre(titre);
        this.description = validerDescription(description);
        this.medias = validerMedias(medias);
        this.auteurId = Objects.requireNonNull(auteurId, "L'auteur de l'actualité est obligatoire");
        this.auteurNom = validerAuteurNom(auteurNom);
        this.tags = validerTags(tags);
        this.statut = statut != null ? statut : StatutActualite.BROUILLON;
    }

    public static Actualite reconstruct(ActualiteId id, CategorieActualite categorie, String titre,
            String description, List<ActualiteMedia> medias, UUID auteurId, String auteurNom, List<String> tags,
            StatutActualite statut, Instant createdAt, Instant updatedAt) {
        return new Actualite(id, categorie, titre, description, medias, auteurId, auteurNom, tags, statut, createdAt,
                updatedAt);
    }

    /**
     * Création d'une nouvelle actualité — toujours à l'état {@code BROUILLON} :
     * la publication est une décision éditoriale explicite et distincte de la
     * saisie.
     */
    public static Actualite creer(CategorieActualite categorie, String titre, String description,
            List<ActualiteMedia> medias, UUID auteurId, String auteurNom, List<String> tags) {
        Instant maintenant = Instant.now();
        return new Actualite(ActualiteId.generate(), categorie, titre, description, medias, auteurId, auteurNom,
                tags, StatutActualite.BROUILLON, maintenant, maintenant);
    }

    // ── Comportements métier ────────────────────────────────────────────

    public void modifierContenu(CategorieActualite categorie, String titre, String description,
            List<ActualiteMedia> medias, List<String> tags) {
        this.categorie = validerCategorie(categorie);
        this.titre = validerTitre(titre);
        this.description = validerDescription(description);
        this.medias = validerMedias(medias);
        this.tags = validerTags(tags);
        markUpdated();
    }

    public void publier() {
        if (this.statut == StatutActualite.PUBLIE) {
            return;
        }
        this.statut = StatutActualite.PUBLIE;
        markUpdated();
    }

    public void desactiver() {
        if (this.statut == StatutActualite.DESACTIVE) {
            return;
        }
        this.statut = StatutActualite.DESACTIVE;
        markUpdated();
    }

    public void remettreEnBrouillon() {
        if (this.statut == StatutActualite.BROUILLON) {
            return;
        }
        this.statut = StatutActualite.BROUILLON;
        markUpdated();
    }

    // ── Validation ───────────────────────────────────────────────────────

    private static CategorieActualite validerCategorie(CategorieActualite categorie) {
        return Objects.requireNonNull(categorie, "La catégorie de l'actualité est obligatoire");
    }

    private static String validerTitre(String titre) {
        Objects.requireNonNull(titre, "Le titre de l'actualité ne peut pas être null");
        String trimmed = titre.trim();
        if (trimmed.isBlank()) {
            throw new IllegalArgumentException("Le titre de l'actualité ne peut pas être vide");
        }
        if (trimmed.length() > TITRE_MAX_LENGTH) {
            throw new IllegalArgumentException("Le titre ne peut pas dépasser " + TITRE_MAX_LENGTH + " caractères");
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

    private static String validerAuteurNom(String auteurNom) {
        Objects.requireNonNull(auteurNom, "Le nom de l'auteur est obligatoire");
        String trimmed = auteurNom.trim();
        if (trimmed.isBlank()) {
            throw new IllegalArgumentException("Le nom de l'auteur ne peut pas être vide");
        }
        if (trimmed.length() > AUTEUR_NOM_MAX_LENGTH) {
            throw new IllegalArgumentException(
                    "Le nom de l'auteur ne peut pas dépasser " + AUTEUR_NOM_MAX_LENGTH + " caractères");
        }
        return trimmed;
    }

    private static List<ActualiteMedia> validerMedias(List<ActualiteMedia> medias) {
        if (medias == null || medias.isEmpty()) {
            return Collections.emptyList();
        }
        if (medias.size() > MEDIAS_MAX) {
            throw new IllegalArgumentException("Une actualité ne peut pas avoir plus de " + MEDIAS_MAX + " médias");
        }
        return List.copyOf(medias);
    }

    private static List<String> validerTags(List<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return Collections.emptyList();
        }
        if (tags.size() > TAGS_MAX) {
            throw new IllegalArgumentException("Une actualité ne peut pas avoir plus de " + TAGS_MAX + " tags");
        }
        List<String> nettoyes = new ArrayList<>();
        for (String tag : tags) {
            if (tag == null || tag.isBlank()) {
                continue;
            }
            String trimmed = tag.trim();
            if (trimmed.length() > TAG_MAX_LENGTH) {
                throw new IllegalArgumentException("Un tag ne peut pas dépasser " + TAG_MAX_LENGTH + " caractères");
            }
            if (!nettoyes.contains(trimmed)) {
                nettoyes.add(trimmed);
            }
        }
        return Collections.unmodifiableList(nettoyes);
    }

    // ── Accesseurs ───────────────────────────────────────────────────────

    public CategorieActualite getCategorie() {
        return categorie;
    }

    public String getTitre() {
        return titre;
    }

    public String getDescription() {
        return description;
    }

    public List<ActualiteMedia> getMedias() {
        return Collections.unmodifiableList(medias);
    }

    public UUID getAuteurId() {
        return auteurId;
    }

    public String getAuteurNom() {
        return auteurNom;
    }

    public List<String> getTags() {
        return Collections.unmodifiableList(tags);
    }

    public StatutActualite getStatut() {
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
