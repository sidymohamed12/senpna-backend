package ministere.sante.senpna.carriere.infrastructure.persistence.mapper;

import ministere.sante.senpna.carriere.domain.model.Candidature;
import ministere.sante.senpna.carriere.domain.model.OpportuniteCarriere;
import ministere.sante.senpna.carriere.domain.valueobject.Civilite;
import ministere.sante.senpna.carriere.domain.valueobject.TypeContrat;
import ministere.sante.senpna.carriere.infrastructure.persistence.entity.CandidatureJpaEntity;
import ministere.sante.senpna.carriere.infrastructure.persistence.entity.OpportuniteCarriereJpaEntity;
import ministere.sante.senpna.shared.domain.valueobject.Email;
import ministere.sante.senpna.shared.domain.valueobject.Phone;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("CandidatureMapper / OpportuniteCarriereMapper — conversion domaine ↔ entité JPA")
class CarriereMappersTest {

    CandidatureMapper candidatureMapper = new CandidatureMapper();
    OpportuniteCarriereMapper opportuniteCarriereMapper = new OpportuniteCarriereMapper();

    @Test
    @DisplayName("CandidatureMapper : aller-retour préserve l'état")
    void candidatureMapper_allerRetourPreserveEtat() {
        Candidature original = Candidature.soumettre(UUID.randomUUID(), Civilite.MME, "Awa Fall",
                Email.of("awa@mail.sn"), Phone.of("+221771234567"), "cv.pdf", "lettre.pdf", "Message", true,
                "Titre", "Entreprise", "rh@e.sn");

        CandidatureJpaEntity entity = candidatureMapper.toNewEntity(original);
        Candidature restaure = candidatureMapper.toDomain(entity);

        assertThat(restaure.getId()).isEqualTo(original.getId());
        assertThat(restaure.getNomComplet()).isEqualTo("Awa Fall");
        assertThat(restaure.getCivilite()).isEqualTo(Civilite.MME);
        assertThat(restaure.isConsentementRgpd()).isTrue();
    }

    @Test
    @DisplayName("OpportuniteCarriereMapper : aller-retour préserve l'état")
    void opportuniteCarriereMapper_allerRetourPreserveEtat() {
        OpportuniteCarriere original = OpportuniteCarriere.creer("Developpeur", "Entreprise X", "Desc", null,
                "Dakar", TypeContrat.CDI, LocalDate.now().plusMonths(2), LocalDate.now().plusMonths(1),
                UUID.randomUUID(), "Auteur", "rh@e.sn");

        OpportuniteCarriereJpaEntity entity = opportuniteCarriereMapper.toNewEntity(original);
        OpportuniteCarriere restaure = opportuniteCarriereMapper.toDomain(entity);

        assertThat(restaure.getId()).isEqualTo(original.getId());
        assertThat(restaure.getTitre()).isEqualTo("Developpeur");
        assertThat(restaure.getTypeContrat()).isEqualTo(TypeContrat.CDI);
        assertThat(restaure.getStatut()).isEqualTo(original.getStatut());
    }

    @Test
    @DisplayName("OpportuniteCarriereMapper.updateEntity() reporte le nouveau contenu sur l'entité existante")
    void opportuniteCarriereMapper_updateEntity_reporteNouveauContenu() {
        OpportuniteCarriere original = OpportuniteCarriere.creer("Ancien titre", "Entreprise X", "Desc", null,
                "Dakar", TypeContrat.CDD, LocalDate.now().plusMonths(2), LocalDate.now().plusMonths(1),
                UUID.randomUUID(), "Auteur", null);
        OpportuniteCarriereJpaEntity entity = opportuniteCarriereMapper.toNewEntity(original);

        original.modifierContenu("Nouveau titre", "Entreprise Y", "Nouvelle desc", null, "Thies", TypeContrat.CDI,
                LocalDate.now().plusMonths(3), LocalDate.now().plusMonths(2), "rh@e.sn");
        OpportuniteCarriereJpaEntity misAJour = opportuniteCarriereMapper.updateEntity(entity, original);

        assertThat(misAJour.getTitre()).isEqualTo("Nouveau titre");
        assertThat(misAJour.getTypeContrat()).isEqualTo(TypeContrat.CDI);
        assertThat(misAJour.getLieu()).isEqualTo("Thies");
    }
}
