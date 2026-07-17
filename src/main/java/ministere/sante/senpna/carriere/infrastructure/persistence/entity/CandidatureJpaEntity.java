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

import ministere.sante.senpna.carriere.domain.valueobject.Civilite;
import ministere.sante.senpna.shared.infrastructure.persistence.entity.BaseJpaEntity;

import java.util.UUID;

@Entity
@Table(name = "candidatures")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(callSuper = false)
public class CandidatureJpaEntity extends BaseJpaEntity {

    @Column(name = "opportunite_id", nullable = false)
    private UUID opportuniteId;

    @Enumerated(EnumType.STRING)
    @Column(name = "civilite", nullable = false, length = 10)
    private Civilite civilite;

    @Column(name = "nom_complet", nullable = false, length = 200)
    private String nomComplet;

    @Column(name = "email", nullable = false, length = 255)
    private String email;

    @Column(name = "telephone", nullable = false, length = 20)
    private String telephone;

    @Column(name = "cv_url", nullable = false, length = 1000)
    private String cvUrl;

    @Column(name = "lettre_motivation_url", length = 1000)
    private String lettreMotivationUrl;

    @Column(name = "message_complementaire", columnDefinition = "TEXT")
    private String messageComplementaire;

    @Column(name = "consentement_rgpd", nullable = false)
    private boolean consentementRgpd;

    private CandidatureJpaEntity(Builder builder) {
        super(builder.id);
        this.opportuniteId = builder.opportuniteId;
        this.civilite = builder.civilite;
        this.nomComplet = builder.nomComplet;
        this.email = builder.email;
        this.telephone = builder.telephone;
        this.cvUrl = builder.cvUrl;
        this.lettreMotivationUrl = builder.lettreMotivationUrl;
        this.messageComplementaire = builder.messageComplementaire;
        this.consentementRgpd = builder.consentementRgpd;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private UUID id;
        private UUID opportuniteId;
        private Civilite civilite;
        private String nomComplet;
        private String email;
        private String telephone;
        private String cvUrl;
        private String lettreMotivationUrl;
        private String messageComplementaire;
        private boolean consentementRgpd;

        private Builder() {
        }

        public Builder id(UUID id) {
            this.id = id;
            return this;
        }

        public Builder opportuniteId(UUID opportuniteId) {
            this.opportuniteId = opportuniteId;
            return this;
        }

        public Builder civilite(Civilite civilite) {
            this.civilite = civilite;
            return this;
        }

        public Builder nomComplet(String nomComplet) {
            this.nomComplet = nomComplet;
            return this;
        }

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder telephone(String telephone) {
            this.telephone = telephone;
            return this;
        }

        public Builder cvUrl(String cvUrl) {
            this.cvUrl = cvUrl;
            return this;
        }

        public Builder lettreMotivationUrl(String lettreMotivationUrl) {
            this.lettreMotivationUrl = lettreMotivationUrl;
            return this;
        }

        public Builder messageComplementaire(String messageComplementaire) {
            this.messageComplementaire = messageComplementaire;
            return this;
        }

        public Builder consentementRgpd(boolean consentementRgpd) {
            this.consentementRgpd = consentementRgpd;
            return this;
        }

        public CandidatureJpaEntity build() {
            return new CandidatureJpaEntity(this);
        }
    }
}
