package ministere.sante.senpna.appeloffre.infrastructure.persistence.entity;

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

import ministere.sante.senpna.appeloffre.domain.valueobject.StatutAppelOffre;
import ministere.sante.senpna.shared.infrastructure.persistence.entity.BaseJpaEntity;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "appels_offres")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(callSuper = false)
public class AppelOffreJpaEntity extends BaseJpaEntity {

    @Column(name = "reference", nullable = false, length = 50)
    private String reference;

    @Column(name = "objet", nullable = false, length = 255)
    private String objet;

    @Column(name = "date_cloture", nullable = false)
    private LocalDate dateCloture;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false, length = 20)
    private StatutAppelOffre statut;

    public AppelOffreJpaEntity(UUID id, String reference, String objet, LocalDate dateCloture,
            StatutAppelOffre statut) {
        super(id);
        this.reference = reference;
        this.objet = objet;
        this.dateCloture = dateCloture;
        this.statut = statut;
    }
}
