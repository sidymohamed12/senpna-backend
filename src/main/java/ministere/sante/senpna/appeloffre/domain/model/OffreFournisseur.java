package ministere.sante.senpna.appeloffre.domain.model;

import ministere.sante.senpna.appeloffre.domain.exception.TransitionStatutOffreInvalideException;
import ministere.sante.senpna.appeloffre.domain.valueobject.AppelOffreId;
import ministere.sante.senpna.appeloffre.domain.valueobject.OffreFournisseurId;
import ministere.sante.senpna.appeloffre.domain.valueobject.StatutOffre;
import ministere.sante.senpna.fournisseur.domain.valueobject.FournisseurId;
import ministere.sante.senpna.shared.domain.model.AggregateRoot;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Offre déposée par un fournisseur en réponse à un {@link AppelOffre}.
 *
 * <p>
 * Agrégat distinct d'{@link AppelOffre} — deux fournisseurs concurrents ne
 * doivent jamais pouvoir se bloquer mutuellement en écrivant sur le même
 * agrégat ; chaque offre a son propre cycle de vie et sa propre
 * concurrence optimiste.
 * </p>
 *
 * <h3>Invariants</h3>
 * <ul>
 * <li>une offre doit comporter au moins une ligne de prix ;</li>
 * <li>seule une offre {@code SOUMISE} peut être retirée, retenue ou
 * rejetée ;</li>
 * <li>{@code RETENUE}, {@code REJETEE} et {@code RETIREE} sont des états
 * terminaux.</li>
 * </ul>
 */
public class OffreFournisseur extends AggregateRoot<OffreFournisseurId> {

    private final AppelOffreId appelOffreId;
    private final FournisseurId fournisseurId;
    private String commentaire;
    private StatutOffre statut;
    private final List<LigneOffre> lignes;

    private OffreFournisseur(Builder builder) {
        super(builder.id, builder.createdAt, builder.updatedAt);
        this.appelOffreId = Objects.requireNonNull(builder.appelOffreId, "L'appel d'offres référencé est obligatoire");
        this.fournisseurId = Objects.requireNonNull(builder.fournisseurId, "Le fournisseur est obligatoire");
        this.commentaire = builder.commentaire;
        this.statut = Objects.requireNonNull(builder.statut, "Le statut est obligatoire");
        this.lignes = new ArrayList<>(Objects.requireNonNull(builder.lignes, "Les lignes ne peuvent pas être null"));
        if (this.lignes.isEmpty()) {
            throw new IllegalArgumentException("Une offre doit contenir au moins une ligne de prix");
        }
    }

    /** Données nécessaires à la soumission d'une nouvelle offre fournisseur. */
    public record SoumissionCommand(AppelOffreId appelOffreId, FournisseurId fournisseurId, String commentaire,
            List<LigneOffre> lignes) {
    }

    public static OffreFournisseur soumettre(SoumissionCommand command) {
        Instant maintenant = Instant.now();
        return builder()
                .id(OffreFournisseurId.generate())
                .appelOffreId(command.appelOffreId())
                .fournisseurId(command.fournisseurId())
                .commentaire(command.commentaire())
                .statut(StatutOffre.SOUMISE)
                .lignes(command.lignes())
                .createdAt(maintenant)
                .updatedAt(maintenant)
                .build();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private OffreFournisseurId id;
        private AppelOffreId appelOffreId;
        private FournisseurId fournisseurId;
        private String commentaire;
        private StatutOffre statut;
        private List<LigneOffre> lignes;
        private Instant createdAt;
        private Instant updatedAt;

        private Builder() {
        }

        public Builder id(OffreFournisseurId id) {
            this.id = id;
            return this;
        }

        public Builder appelOffreId(AppelOffreId appelOffreId) {
            this.appelOffreId = appelOffreId;
            return this;
        }

        public Builder fournisseurId(FournisseurId fournisseurId) {
            this.fournisseurId = fournisseurId;
            return this;
        }

        public Builder commentaire(String commentaire) {
            this.commentaire = commentaire;
            return this;
        }

        public Builder statut(StatutOffre statut) {
            this.statut = statut;
            return this;
        }

        public Builder lignes(List<LigneOffre> lignes) {
            this.lignes = lignes;
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

        public OffreFournisseur build() {
            return new OffreFournisseur(this);
        }
    }

    // ── Comportements métier ────────────────────────────────────────────

    public void retirer() {
        exigerStatut(StatutOffre.SOUMISE, "retirer");
        this.statut = StatutOffre.RETIREE;
        markUpdated();
    }

    public void retenir() {
        exigerStatut(StatutOffre.SOUMISE, "retenir");
        this.statut = StatutOffre.RETENUE;
        markUpdated();
    }

    public void rejeter() {
        exigerStatut(StatutOffre.SOUMISE, "rejeter");
        this.statut = StatutOffre.REJETEE;
        markUpdated();
    }

    public boolean appartientA(FournisseurId candidat) {
        return this.fournisseurId.equals(candidat);
    }

    private void exigerStatut(StatutOffre attendu, String action) {
        if (this.statut != attendu) {
            throw new TransitionStatutOffreInvalideException(this.statut, action);
        }
    }

    // ── Accesseurs ───────────────────────────────────────────────────────

    public AppelOffreId getAppelOffreId() {
        return appelOffreId;
    }

    public FournisseurId getFournisseurId() {
        return fournisseurId;
    }

    public String getCommentaire() {
        return commentaire;
    }

    public StatutOffre getStatut() {
        return statut;
    }

    public List<LigneOffre> getLignes() {
        return Collections.unmodifiableList(lignes);
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
