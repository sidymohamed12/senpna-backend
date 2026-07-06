package ministere.sante.senpna.actualite.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import ministere.sante.senpna.actualite.domain.valueobject.TypeMedia;
import ministere.sante.senpna.shared.infrastructure.persistence.entity.BaseJpaEntity;

import java.util.UUID;

@Entity
@Table(name = "actualite_medias")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(callSuper = false)
public class ActualiteMediaJpaEntity extends BaseJpaEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actualite_id", nullable = false)
    @EqualsAndHashCode.Exclude
    private ActualiteJpaEntity actualite;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private TypeMedia type;

    @Column(name = "url", nullable = false, length = 1000)
    private String url;

    @Column(name = "ordre", nullable = false)
    private int ordre;

    public ActualiteMediaJpaEntity(UUID id, ActualiteJpaEntity actualite, TypeMedia type, String url, int ordre) {
        super(id);
        this.actualite = actualite;
        this.type = type;
        this.url = url;
        this.ordre = ordre;
    }
}
