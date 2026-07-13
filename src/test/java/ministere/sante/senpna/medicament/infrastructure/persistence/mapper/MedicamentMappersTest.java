package ministere.sante.senpna.medicament.infrastructure.persistence.mapper;

import ministere.sante.senpna.medicament.domain.model.Conditionnement;
import ministere.sante.senpna.medicament.domain.model.Famille;
import ministere.sante.senpna.medicament.domain.model.Forme;
import ministere.sante.senpna.medicament.domain.model.Medicament;
import ministere.sante.senpna.medicament.domain.valueobject.FamilleId;
import ministere.sante.senpna.medicament.domain.valueobject.FormeId;
import ministere.sante.senpna.medicament.domain.valueobject.MedicamentId;
import ministere.sante.senpna.medicament.domain.valueobject.VoieAdministration;
import ministere.sante.senpna.medicament.infrastructure.persistence.entity.ConditionnementJpaEntity;
import ministere.sante.senpna.medicament.infrastructure.persistence.entity.FamilleJpaEntity;
import ministere.sante.senpna.medicament.infrastructure.persistence.entity.FormeJpaEntity;
import ministere.sante.senpna.medicament.infrastructure.persistence.entity.MedicamentJpaEntity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Mappers de persistence du module medicament")
class MedicamentMappersTest {

    FamilleMapper familleMapper = new FamilleMapper();
    FormeMapper formeMapper = new FormeMapper();
    MedicamentMapper medicamentMapper = new MedicamentMapper();
    ConditionnementMapper conditionnementMapper = new ConditionnementMapper();

    @Test
    @DisplayName("FamilleMapper : aller-retour préserve l'état")
    void familleMapper_allerRetour() {
        Famille original = Famille.creer("ANTIBIO", "Antibiotiques", "Desc");

        FamilleJpaEntity entity = familleMapper.toEntity(original);
        Famille restaure = familleMapper.toDomain(entity);

        assertThat(restaure.getId()).isEqualTo(original.getId());
        assertThat(restaure.getCode()).isEqualTo("ANTIBIO");
    }

    @Test
    @DisplayName("FormeMapper : aller-retour préserve l'état")
    void formeMapper_allerRetour() {
        Forme original = Forme.creer("COMP", "Comprimé", "Desc");

        FormeJpaEntity entity = formeMapper.toEntity(original);
        Forme restaure = formeMapper.toDomain(entity);

        assertThat(restaure.getId()).isEqualTo(original.getId());
        assertThat(restaure.getCode()).isEqualTo("COMP");
    }

    @Test
    @DisplayName("MedicamentMapper : aller-retour préserve l'état")
    void medicamentMapper_allerRetour() {
        Medicament original = Medicament.creer("MED-1", "Zolpidem", "Zolpidem", "10mg", FormeId.generate(),
                FamilleId.generate(), VoieAdministration.ORALE, null, null, null, true, "Sanofi", null, null);

        MedicamentJpaEntity entity = medicamentMapper.toEntity(original);
        Medicament restaure = medicamentMapper.toDomain(entity);

        assertThat(restaure.getId()).isEqualTo(original.getId());
        assertThat(restaure.getNomCommercial()).isEqualTo("Zolpidem");
        assertThat(restaure.getVoieAdministration()).isEqualTo(VoieAdministration.ORALE);
        assertThat(restaure.isNecessiteOrdonnance()).isTrue();
    }

    @Test
    @DisplayName("ConditionnementMapper : aller-retour préserve l'état")
    void conditionnementMapper_allerRetour() {
        Conditionnement original = Conditionnement.creer(MedicamentId.generate(), "Boite de 10", 1,
                BigDecimal.ONE, true, BigDecimal.TEN, new BigDecimal("15"));

        ConditionnementJpaEntity entity = conditionnementMapper.toEntity(original);
        Conditionnement restaure = conditionnementMapper.toDomain(entity);

        assertThat(restaure.getId()).isEqualTo(original.getId());
        assertThat(restaure.getNom()).isEqualTo("Boite de 10");
        assertThat(restaure.isEstUniteBase()).isTrue();
        assertThat(restaure.getPrixVente()).isEqualByComparingTo("15");
    }
}
