package ministere.sante.senpna.commandeachat.domain.model;

import ministere.sante.senpna.commandeachat.domain.exception.LigneCommandeAchatIntrouvableException;
import ministere.sante.senpna.commandeachat.domain.exception.TransitionStatutCommandeAchatInvalideException;
import ministere.sante.senpna.commandeachat.domain.valueobject.CommandeAchatId;
import ministere.sante.senpna.commandeachat.domain.valueobject.LigneCommandeAchatId;
import ministere.sante.senpna.commandeachat.domain.valueobject.StatutCommandeAchat;
import ministere.sante.senpna.fournisseur.domain.valueobject.FournisseurId;
import ministere.sante.senpna.organisation.domain.valueobject.EntrepotId;
import ministere.sante.senpna.shared.domain.model.AggregateRoot;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Bon de commande d'achat fournisseur — cf. doc. métier flows CAS 1 et
 * « Gestion des Commandes d'Achat » / « Suivi des Livraisons » côté
 * espace fournisseur : réception et traitement du bon de commande par le
 * fournisseur (accusé de réception, confirmation de délai, avis
 * d'expédition), puis réception par la PNA.
 *
 * <h3>Invariants</h3>
 * <ul>
 * <li>une commande doit contenir au moins une ligne ;</li>
 * <li>chaque transition d'état ne peut avoir lieu que depuis le(s)
 * statut(s) prévu(s) par le workflow (cf. {@link StatutCommandeAchat}) ;</li>
 * <li>une commande {@code EXPEDIEE} ou postérieure ne peut plus être
 * modifiée ni annulée (cf. doc. métier, règles « Commandes »).</li>
 * </ul>
 */
public class CommandeAchat extends AggregateRoot<CommandeAchatId> {

    private static final int REFERENCE_MAX_LENGTH = 50;

    private String reference;
    private final FournisseurId fournisseurId;
    private final EntrepotId entrepotDestinationId;
    private StatutCommandeAchat statut;
    private final List<LigneCommandeAchat> lignes;

    private Instant dateAccuseReceptionFournisseur;
    private Integer delaiLivraisonConfirmeJours;
    private LocalDate dateLivraisonConfirmee;
    private AvisExpedition avisExpedition;
    private String motifRejet;
    private String commentaire;

    private CommandeAchat(Builder builder) {
        super(builder.id, builder.createdAt, builder.updatedAt);
        this.reference = validerReference(builder.reference);
        this.fournisseurId = Objects.requireNonNull(builder.fournisseurId, "Le fournisseur est obligatoire");
        this.entrepotDestinationId = Objects.requireNonNull(builder.entrepotDestinationId,
                "L'entrepôt de destination est obligatoire");
        this.statut = Objects.requireNonNull(builder.statut, "Le statut est obligatoire");
        this.lignes = new ArrayList<>(Objects.requireNonNull(builder.lignes, "Les lignes ne peuvent pas être null"));
        if (this.lignes.isEmpty()) {
            throw new IllegalArgumentException("Une commande d'achat doit contenir au moins une ligne");
        }
        this.dateAccuseReceptionFournisseur = builder.dateAccuseReceptionFournisseur;
        this.delaiLivraisonConfirmeJours = builder.delaiLivraisonConfirmeJours;
        this.dateLivraisonConfirmee = builder.dateLivraisonConfirmee;
        this.avisExpedition = builder.avisExpedition;
        this.motifRejet = builder.motifRejet;
        this.commentaire = builder.commentaire;
    }

    /** Données nécessaires à la création d'une nouvelle commande d'achat. */
    public record CreationCommand(String reference, FournisseurId fournisseurId, EntrepotId entrepotDestinationId,
            List<LigneCommandeAchat> lignes, String commentaire) {
    }

    public static CommandeAchat creer(CreationCommand command) {
        Instant maintenant = Instant.now();
        return builder()
                .id(CommandeAchatId.generate())
                .reference(command.reference())
                .fournisseurId(command.fournisseurId())
                .entrepotDestinationId(command.entrepotDestinationId())
                .statut(StatutCommandeAchat.EN_ATTENTE_VALIDATION)
                .lignes(command.lignes())
                .commentaire(command.commentaire())
                .createdAt(maintenant)
                .updatedAt(maintenant)
                .build();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private CommandeAchatId id;
        private String reference;
        private FournisseurId fournisseurId;
        private EntrepotId entrepotDestinationId;
        private StatutCommandeAchat statut;
        private List<LigneCommandeAchat> lignes;
        private Instant dateAccuseReceptionFournisseur;
        private Integer delaiLivraisonConfirmeJours;
        private LocalDate dateLivraisonConfirmee;
        private AvisExpedition avisExpedition;
        private String motifRejet;
        private String commentaire;
        private Instant createdAt;
        private Instant updatedAt;

        private Builder() {
        }

        public Builder id(CommandeAchatId id) {
            this.id = id;
            return this;
        }

        public Builder reference(String reference) {
            this.reference = reference;
            return this;
        }

        public Builder fournisseurId(FournisseurId fournisseurId) {
            this.fournisseurId = fournisseurId;
            return this;
        }

        public Builder entrepotDestinationId(EntrepotId entrepotDestinationId) {
            this.entrepotDestinationId = entrepotDestinationId;
            return this;
        }

        public Builder statut(StatutCommandeAchat statut) {
            this.statut = statut;
            return this;
        }

        public Builder lignes(List<LigneCommandeAchat> lignes) {
            this.lignes = lignes;
            return this;
        }

        public Builder dateAccuseReceptionFournisseur(Instant dateAccuseReceptionFournisseur) {
            this.dateAccuseReceptionFournisseur = dateAccuseReceptionFournisseur;
            return this;
        }

        public Builder delaiLivraisonConfirmeJours(Integer delaiLivraisonConfirmeJours) {
            this.delaiLivraisonConfirmeJours = delaiLivraisonConfirmeJours;
            return this;
        }

        public Builder dateLivraisonConfirmee(LocalDate dateLivraisonConfirmee) {
            this.dateLivraisonConfirmee = dateLivraisonConfirmee;
            return this;
        }

        public Builder avisExpedition(AvisExpedition avisExpedition) {
            this.avisExpedition = avisExpedition;
            return this;
        }

        public Builder motifRejet(String motifRejet) {
            this.motifRejet = motifRejet;
            return this;
        }

        public Builder commentaire(String commentaire) {
            this.commentaire = commentaire;
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

        public CommandeAchat build() {
            return new CommandeAchat(this);
        }
    }

    // ── PNA : validation interne ─────────────────────────────────────────

    public void validerInterne() {
        exigerStatut(StatutCommandeAchat.EN_ATTENTE_VALIDATION, "valider");
        this.statut = StatutCommandeAchat.VALIDEE;
        markUpdated();
    }

    public void rejeter(String motif) {
        exigerStatut(StatutCommandeAchat.EN_ATTENTE_VALIDATION, "rejeter");
        this.motifRejet = motif;
        this.statut = StatutCommandeAchat.REJETEE;
        markUpdated();
    }

    public void annuler() {
        if (statut == StatutCommandeAchat.EXPEDIEE || statut == StatutCommandeAchat.PARTIELLEMENT_RECEPTIONNEE
                || statut == StatutCommandeAchat.RECEPTIONNEE || statut == StatutCommandeAchat.REJETEE
                || statut == StatutCommandeAchat.ANNULEE) {
            throw new TransitionStatutCommandeAchatInvalideException(statut, "annuler");
        }
        this.statut = StatutCommandeAchat.ANNULEE;
        markUpdated();
    }

    // ── Fournisseur : réception et traitement du bon de commande ────────

    /**
     * Accusé de réception du bon de commande par le fournisseur — ne
     * change pas le statut, se contente d'horodater la prise de
     * connaissance (cf. doc. métier « Accusé de réception »).
     */
    public void accuserReception() {
        exigerStatut(StatutCommandeAchat.VALIDEE, "accuser réception");
        this.dateAccuseReceptionFournisseur = Instant.now();
        markUpdated();
    }

    /**
     * Confirmation du délai de livraison par le fournisseur — fait passer
     * la commande en préparation (cf. doc. métier « confirmation des
     * délais »).
     */
    public void confirmerDelaiLivraison(int delaiJours, LocalDate dateLivraisonConfirmee) {
        exigerStatut(StatutCommandeAchat.VALIDEE, "confirmer le délai de livraison");
        if (delaiJours <= 0) {
            throw new IllegalArgumentException("Le délai de livraison confirmé doit être strictement positif");
        }
        this.delaiLivraisonConfirmeJours = delaiJours;
        this.dateLivraisonConfirmee = dateLivraisonConfirmee;
        this.statut = StatutCommandeAchat.EN_TRANSIT;
        markUpdated();
    }

    /**
     * Génère l'avis d'expédition — le fournisseur renseigne, pour chaque
     * ligne expédiée, le lot, les dates de fabrication/péremption, le
     * certificat d'analyse et la quantité expédiée (cf. doc. métier
     * « Suivi des Livraisons »).
     */
    public void genererAvisExpedition(AvisExpedition avis, List<InfoExpeditionLigne> infosLignes) {
        exigerStatut(StatutCommandeAchat.EN_TRANSIT, "générer l'avis d'expédition");
        Objects.requireNonNull(avis, "L'avis d'expédition est obligatoire");
        if (infosLignes == null || infosLignes.isEmpty()) {
            throw new IllegalArgumentException("Au moins une ligne doit être expédiée");
        }
        for (InfoExpeditionLigne info : infosLignes) {
            LigneCommandeAchat ligne = trouverLigne(info.ligneId());
            ligne.renseignerExpedition(info.numeroLot(), info.dateFabrication(), info.dateExpiration(),
                    info.certificatAnalyseUrl(), info.quantiteExpediee());
        }
        this.avisExpedition = avis;
        this.statut = StatutCommandeAchat.EXPEDIEE;
        markUpdated();
    }

    // ── PNA : réception ──────────────────────────────────────────────────

    /**
     * Enregistre une réception (totale ou partielle) — met à jour le
     * statut global selon que toutes les lignes sont ou non complètement
     * réceptionnées (cf. règles métier « Réceptions »).
     */
    public void receptionner(List<InfoReceptionLigne> infosLignes) {
        if (statut != StatutCommandeAchat.EXPEDIEE && statut != StatutCommandeAchat.PARTIELLEMENT_RECEPTIONNEE) {
            throw new TransitionStatutCommandeAchatInvalideException(statut, "réceptionner");
        }
        if (infosLignes == null || infosLignes.isEmpty()) {
            throw new IllegalArgumentException("Au moins une ligne doit être réceptionnée");
        }
        for (InfoReceptionLigne info : infosLignes) {
            LigneCommandeAchat ligne = trouverLigne(info.ligneId());
            ligne.receptionner(info.quantiteRecue(), info.quantiteRefusee(), info.motifRefus());
        }
        boolean toutesCompletes = lignes.stream().allMatch(LigneCommandeAchat::estCompletementReceptionnee);
        this.statut = toutesCompletes ? StatutCommandeAchat.RECEPTIONNEE
                : StatutCommandeAchat.PARTIELLEMENT_RECEPTIONNEE;
        markUpdated();
    }

    private LigneCommandeAchat trouverLigne(LigneCommandeAchatId ligneId) {
        return lignes.stream()
                .filter(l -> l.getId().equals(ligneId))
                .findFirst()
                .orElseThrow(LigneCommandeAchatIntrouvableException::new);
    }

    private void exigerStatut(StatutCommandeAchat attendu, String action) {
        if (this.statut != attendu) {
            throw new TransitionStatutCommandeAchatInvalideException(this.statut, action);
        }
    }

    private static String validerReference(String reference) {
        Objects.requireNonNull(reference, "La référence ne peut pas être null");
        String trimmed = reference.trim();
        if (trimmed.isBlank()) {
            throw new IllegalArgumentException("La référence ne peut pas être vide");
        }
        if (trimmed.length() > REFERENCE_MAX_LENGTH) {
            throw new IllegalArgumentException(
                    "La référence ne peut pas dépasser " + REFERENCE_MAX_LENGTH + " caractères");
        }
        return trimmed;
    }

    // ── Accesseurs ───────────────────────────────────────────────────────

    public String getReference() {
        return reference;
    }

    public FournisseurId getFournisseurId() {
        return fournisseurId;
    }

    public EntrepotId getEntrepotDestinationId() {
        return entrepotDestinationId;
    }

    public StatutCommandeAchat getStatut() {
        return statut;
    }

    public List<LigneCommandeAchat> getLignes() {
        return Collections.unmodifiableList(lignes);
    }

    public Instant getDateAccuseReceptionFournisseur() {
        return dateAccuseReceptionFournisseur;
    }

    public Integer getDelaiLivraisonConfirmeJours() {
        return delaiLivraisonConfirmeJours;
    }

    public LocalDate getDateLivraisonConfirmee() {
        return dateLivraisonConfirmee;
    }

    public AvisExpedition getAvisExpedition() {
        return avisExpedition;
    }

    public String getMotifRejet() {
        return motifRejet;
    }

    public String getCommentaire() {
        return commentaire;
    }

    public boolean appartientA(FournisseurId candidat) {
        return this.fournisseurId.equals(candidat);
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    /** Entrée utilitaire pour {@link #genererAvisExpedition}. */
    public record InfoExpeditionLigne(LigneCommandeAchatId ligneId, String numeroLot, LocalDate dateFabrication,
            LocalDate dateExpiration, String certificatAnalyseUrl, BigDecimal quantiteExpediee) {
    }

    /** Entrée utilitaire pour {@link #receptionner}. */
    public record InfoReceptionLigne(LigneCommandeAchatId ligneId, BigDecimal quantiteRecue,
            BigDecimal quantiteRefusee, String motifRefus) {
    }
}
