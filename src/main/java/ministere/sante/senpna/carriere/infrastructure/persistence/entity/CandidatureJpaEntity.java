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

    public CandidatureJpaEntity(UUID id, UUID opportuniteId, Civilite civilite, String nomComplet, String email,
            String telephone, String cvUrl, String lettreMotivationUrl, String messageComplementaire,
            boolean consentementRgpd) {
        super(id);
        this.opportuniteId = opportuniteId;
        this.civilite = civilite;
        this.nomComplet = nomComplet;
        this.email = email;
        this.telephone = telephone;
        this.cvUrl = cvUrl;
        this.lettreMotivationUrl = lettreMotivationUrl;
        this.messageComplementaire = messageComplementaire;
        this.consentementRgpd = consentementRgpd;
    }
}
