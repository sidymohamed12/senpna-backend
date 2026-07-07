package ministere.sante.senpna.carriere.infrastructure.persistence.entity;

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

import ministere.sante.senpna.carriere.domain.valueobject.StatutOpportunite;
import ministere.sante.senpna.carriere.domain.valueobject.TypeContrat;
import ministere.sante.senpna.shared.infrastructure.persistence.entity.BaseJpaEntity;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "opportunites_carriere")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(callSuper = false)
public class OpportuniteCarriereJpaEntity extends BaseJpaEntity {

    @Column(name = "titre", nullable = false, length = 200)
    private String titre;

    @Column(name = "nom_entreprise", nullable = false, length = 200)
    private String nomEntreprise;

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "fiche_de_poste_url", length = 1000)
    private String ficheDePosteUrl;

    @Column(name = "lieu", nullable = false, length = 200)
    private String lieu;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_contrat", nullable = false, length = 30)
    private TypeContrat typeContrat;

    @Column(name = "date_debut")
    private LocalDate dateDebut;

    @Column(name = "date_limite_candidature", nullable = false)
    private LocalDate dateLimiteCandidature;

    @Column(name = "auteur_id", nullable = false)
    private UUID auteurId;

    @Column(name = "auteur_nom", nullable = false, length = 200)
    private String auteurNom;

    @Column(name = "email_contact", length = 255)
    private String emailContact;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false, length = 20)
    private StatutOpportunite statut;

    public OpportuniteCarriereJpaEntity(UUID id, String titre, String nomEntreprise, String description,
            String ficheDePosteUrl, String lieu, TypeContrat typeContrat, LocalDate dateDebut,
            LocalDate dateLimiteCandidature, UUID auteurId, String auteurNom, String emailContact,
            StatutOpportunite statut) {
        super(id);
        this.titre = titre;
        this.nomEntreprise = nomEntreprise;
        this.description = description;
        this.ficheDePosteUrl = ficheDePosteUrl;
        this.lieu = lieu;
        this.typeContrat = typeContrat;
        this.dateDebut = dateDebut;
        this.dateLimiteCandidature = dateLimiteCandidature;
        this.auteurId = auteurId;
        this.auteurNom = auteurNom;
        this.emailContact = emailContact;
        this.statut = statut;
    }
}
