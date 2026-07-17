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

    private OpportuniteCarriereJpaEntity(Builder builder) {
        super(builder.id);
        this.titre = builder.titre;
        this.nomEntreprise = builder.nomEntreprise;
        this.description = builder.description;
        this.ficheDePosteUrl = builder.ficheDePosteUrl;
        this.lieu = builder.lieu;
        this.typeContrat = builder.typeContrat;
        this.dateDebut = builder.dateDebut;
        this.dateLimiteCandidature = builder.dateLimiteCandidature;
        this.auteurId = builder.auteurId;
        this.auteurNom = builder.auteurNom;
        this.emailContact = builder.emailContact;
        this.statut = builder.statut;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private UUID id;
        private String titre;
        private String nomEntreprise;
        private String description;
        private String ficheDePosteUrl;
        private String lieu;
        private TypeContrat typeContrat;
        private LocalDate dateDebut;
        private LocalDate dateLimiteCandidature;
        private UUID auteurId;
        private String auteurNom;
        private String emailContact;
        private StatutOpportunite statut;

        private Builder() {
        }

        public Builder id(UUID id) {
            this.id = id;
            return this;
        }

        public Builder titre(String titre) {
            this.titre = titre;
            return this;
        }

        public Builder nomEntreprise(String nomEntreprise) {
            this.nomEntreprise = nomEntreprise;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder ficheDePosteUrl(String ficheDePosteUrl) {
            this.ficheDePosteUrl = ficheDePosteUrl;
            return this;
        }

        public Builder lieu(String lieu) {
            this.lieu = lieu;
            return this;
        }

        public Builder typeContrat(TypeContrat typeContrat) {
            this.typeContrat = typeContrat;
            return this;
        }

        public Builder dateDebut(LocalDate dateDebut) {
            this.dateDebut = dateDebut;
            return this;
        }

        public Builder dateLimiteCandidature(LocalDate dateLimiteCandidature) {
            this.dateLimiteCandidature = dateLimiteCandidature;
            return this;
        }

        public Builder auteurId(UUID auteurId) {
            this.auteurId = auteurId;
            return this;
        }

        public Builder auteurNom(String auteurNom) {
            this.auteurNom = auteurNom;
            return this;
        }

        public Builder emailContact(String emailContact) {
            this.emailContact = emailContact;
            return this;
        }

        public Builder statut(StatutOpportunite statut) {
            this.statut = statut;
            return this;
        }

        public OpportuniteCarriereJpaEntity build() {
            return new OpportuniteCarriereJpaEntity(this);
        }
    }
}
