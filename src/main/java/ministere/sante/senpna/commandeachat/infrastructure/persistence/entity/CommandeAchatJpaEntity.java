package ministere.sante.senpna.commandeachat.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import ministere.sante.senpna.commandeachat.domain.valueobject.StatutCommandeAchat;
import ministere.sante.senpna.shared.infrastructure.persistence.entity.BaseJpaEntity;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "commandes_achat")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(callSuper = false)
public class CommandeAchatJpaEntity extends BaseJpaEntity {

    @Column(name = "reference", nullable = false, length = 50)
    private String reference;

    @Column(name = "fournisseur_id", nullable = false)
    private UUID fournisseurId;

    @Column(name = "entrepot_destination_id", nullable = false)
    private UUID entrepotDestinationId;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false, length = 30)
    private StatutCommandeAchat statut;

    @Column(name = "date_accuse_reception_fournisseur")
    private Instant dateAccuseReceptionFournisseur;

    @Column(name = "delai_livraison_confirme_jours")
    private Integer delaiLivraisonConfirmeJours;

    @Column(name = "date_livraison_confirmee")
    private LocalDate dateLivraisonConfirmee;

    @Column(name = "avis_date_expedition")
    private LocalDate avisDateExpedition;

    @Column(name = "avis_transporteur", length = 150)
    private String avisTransporteur;

    @Column(name = "avis_numero_suivi", length = 100)
    private String avisNumeroSuivi;

    @Column(name = "avis_date_livraison_estimee")
    private LocalDate avisDateLivraisonEstimee;

    @Column(name = "motif_rejet", length = 500)
    private String motifRejet;

    @Column(name = "commentaire", length = 1000)
    private String commentaire;

    private CommandeAchatJpaEntity(Builder builder) {
        super(builder.id);
        this.reference = builder.reference;
        this.fournisseurId = builder.fournisseurId;
        this.entrepotDestinationId = builder.entrepotDestinationId;
        this.statut = builder.statut;
        this.dateAccuseReceptionFournisseur = builder.dateAccuseReceptionFournisseur;
        this.delaiLivraisonConfirmeJours = builder.delaiLivraisonConfirmeJours;
        this.dateLivraisonConfirmee = builder.dateLivraisonConfirmee;
        this.avisDateExpedition = builder.avisDateExpedition;
        this.avisTransporteur = builder.avisTransporteur;
        this.avisNumeroSuivi = builder.avisNumeroSuivi;
        this.avisDateLivraisonEstimee = builder.avisDateLivraisonEstimee;
        this.motifRejet = builder.motifRejet;
        this.commentaire = builder.commentaire;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private UUID id;
        private String reference;
        private UUID fournisseurId;
        private UUID entrepotDestinationId;
        private StatutCommandeAchat statut;
        private Instant dateAccuseReceptionFournisseur;
        private Integer delaiLivraisonConfirmeJours;
        private LocalDate dateLivraisonConfirmee;
        private LocalDate avisDateExpedition;
        private String avisTransporteur;
        private String avisNumeroSuivi;
        private LocalDate avisDateLivraisonEstimee;
        private String motifRejet;
        private String commentaire;

        private Builder() {
        }

        public Builder id(UUID id) {
            this.id = id;
            return this;
        }

        public Builder reference(String reference) {
            this.reference = reference;
            return this;
        }

        public Builder fournisseurId(UUID fournisseurId) {
            this.fournisseurId = fournisseurId;
            return this;
        }

        public Builder entrepotDestinationId(UUID entrepotDestinationId) {
            this.entrepotDestinationId = entrepotDestinationId;
            return this;
        }

        public Builder statut(StatutCommandeAchat statut) {
            this.statut = statut;
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

        public Builder avisDateExpedition(LocalDate avisDateExpedition) {
            this.avisDateExpedition = avisDateExpedition;
            return this;
        }

        public Builder avisTransporteur(String avisTransporteur) {
            this.avisTransporteur = avisTransporteur;
            return this;
        }

        public Builder avisNumeroSuivi(String avisNumeroSuivi) {
            this.avisNumeroSuivi = avisNumeroSuivi;
            return this;
        }

        public Builder avisDateLivraisonEstimee(LocalDate avisDateLivraisonEstimee) {
            this.avisDateLivraisonEstimee = avisDateLivraisonEstimee;
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

        public CommandeAchatJpaEntity build() {
            return new CommandeAchatJpaEntity(this);
        }
    }

}
