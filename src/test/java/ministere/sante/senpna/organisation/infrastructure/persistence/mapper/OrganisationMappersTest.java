package ministere.sante.senpna.organisation.infrastructure.persistence.mapper;

import ministere.sante.senpna.organisation.domain.model.Entrepot;
import ministere.sante.senpna.organisation.domain.model.Region;
import ministere.sante.senpna.organisation.domain.model.StructureSanitaire;
import ministere.sante.senpna.organisation.domain.valueobject.RegionId;
import ministere.sante.senpna.organisation.domain.valueobject.TypeEntrepot;
import ministere.sante.senpna.organisation.domain.valueobject.TypeStructureSanitaire;
import ministere.sante.senpna.organisation.infrastructure.persistence.entity.EntrepotJpaEntity;
import ministere.sante.senpna.organisation.infrastructure.persistence.entity.RegionJpaEntity;
import ministere.sante.senpna.organisation.infrastructure.persistence.entity.StructureSanitaireJpaEntity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Mappers de persistence du module organisation")
class OrganisationMappersTest {

    RegionMapper regionMapper = new RegionMapper();
    EntrepotMapper entrepotMapper = new EntrepotMapper();
    StructureSanitaireMapper structureSanitaireMapper = new StructureSanitaireMapper();

    @Test
    @DisplayName("RegionMapper : aller-retour préserve l'état")
    void regionMapper_allerRetourPreserveEtat() {
        Region original = Region.creer("THIES", "Thies");

        RegionJpaEntity entity = regionMapper.toEntity(original);
        Region restaure = regionMapper.toDomain(entity);

        assertThat(restaure.getId()).isEqualTo(original.getId());
        assertThat(restaure.getCode()).isEqualTo("THIES");
        assertThat(restaure.isActif()).isTrue();
    }

    @Test
    @DisplayName("EntrepotMapper : aller-retour préserve l'état, y compris la région")
    void entrepotMapper_allerRetourPreserveEtatAvecRegion() {
        RegionId regionId = RegionId.generate();
        Entrepot original = Entrepot.creerPra(new Entrepot.CreationCommand("PRA-THIES", "PRA Thies", regionId, "Adresse", "771234567"));

        EntrepotJpaEntity entity = entrepotMapper.toEntity(original);
        Entrepot restaure = entrepotMapper.toDomain(entity);

        assertThat(restaure.getId()).isEqualTo(original.getId());
        assertThat(restaure.getType()).isEqualTo(TypeEntrepot.PRA);
        assertThat(restaure.getRegionId()).isEqualTo(regionId);
    }

    @Test
    @DisplayName("EntrepotMapper : regionId absent (PNA central) → toEntity()/toDomain() gèrent le null")
    void entrepotMapper_regionIdAbsent_gereLeNull() {
        EntrepotJpaEntity entity = EntrepotJpaEntity.builder()
            .id(UUID.randomUUID())
            .code("PNA-CENTRAL")
            .nom("PNA Central")
            .type(TypeEntrepot.PNA_CENTRAL)
            .regionId(null)
            .adresse(null)
            .telephone(null)
            .responsableUserId(null)
            .actif(true)
            .build();

        Instant now = Instant.now();
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);

        Entrepot domaine = entrepotMapper.toDomain(entity);

        assertThat(domaine.getRegionId()).isNull();
        assertThat(entrepotMapper.toEntity(domaine).getRegionId()).isNull();
    }

    @Test
    @DisplayName("StructureSanitaireMapper : aller-retour préserve l'état")
    void structureSanitaireMapper_allerRetourPreserveEtat() {
        StructureSanitaire original = StructureSanitaire.creer(new StructureSanitaire.CreationCommand("PS-FANN", "Poste de sante Fann",
                TypeStructureSanitaire.POSTE_SANTE, RegionId.generate(), "District Nord", "Adresse",
                "771234567", "ps@sante.sn", "Ndiaye", "Fatou"));

        StructureSanitaireJpaEntity entity = structureSanitaireMapper.toEntity(original);
        StructureSanitaire restaure = structureSanitaireMapper.toDomain(entity);

        assertThat(restaure.getId()).isEqualTo(original.getId());
        assertThat(restaure.getResponsableNom()).isEqualTo("Ndiaye");
        assertThat(restaure.getStatutAdhesion()).isEqualTo(original.getStatutAdhesion());
    }
}
