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

    public CommandeAchatJpaEntity(UUID id, String reference, UUID fournisseurId, UUID entrepotDestinationId,
            StatutCommandeAchat statut, Instant dateAccuseReceptionFournisseur, Integer delaiLivraisonConfirmeJours,
            LocalDate dateLivraisonConfirmee, LocalDate avisDateExpedition, String avisTransporteur,
            String avisNumeroSuivi, LocalDate avisDateLivraisonEstimee, String motifRejet, String commentaire) {
        super(id);
        this.reference = reference;
        this.fournisseurId = fournisseurId;
        this.entrepotDestinationId = entrepotDestinationId;
        this.statut = statut;
        this.dateAccuseReceptionFournisseur = dateAccuseReceptionFournisseur;
        this.delaiLivraisonConfirmeJours = delaiLivraisonConfirmeJours;
        this.dateLivraisonConfirmee = dateLivraisonConfirmee;
        this.avisDateExpedition = avisDateExpedition;
        this.avisTransporteur = avisTransporteur;
        this.avisNumeroSuivi = avisNumeroSuivi;
        this.avisDateLivraisonEstimee = avisDateLivraisonEstimee;
        this.motifRejet = motifRejet;
        this.commentaire = commentaire;
    }
}
