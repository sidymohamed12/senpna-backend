package ministere.sante.senpna.carriere.infrastructure.persistence.adapter;

import ministere.sante.senpna.carriere.domain.criteria.CandidatureSearchCriteria;
import ministere.sante.senpna.carriere.domain.criteria.OpportuniteCarriereSearchCriteria;
import ministere.sante.senpna.carriere.domain.model.Candidature;
import ministere.sante.senpna.carriere.domain.model.OpportuniteCarriere;
import ministere.sante.senpna.carriere.domain.valueobject.Civilite;
import ministere.sante.senpna.carriere.domain.valueobject.StatutOpportunite;
import ministere.sante.senpna.carriere.domain.valueobject.TypeContrat;
import ministere.sante.senpna.carriere.infrastructure.persistence.mapper.CandidatureMapper;
import ministere.sante.senpna.carriere.infrastructure.persistence.mapper.OpportuniteCarriereMapper;
import ministere.sante.senpna.carriere.infrastructure.persistence.repository.CandidatureJpaRepository;
import ministere.sante.senpna.carriere.infrastructure.persistence.repository.OpportuniteCarriereJpaRepository;
import ministere.sante.senpna.shared.domain.valueobject.Email;
import ministere.sante.senpna.shared.domain.valueobject.PageRequest;
import ministere.sante.senpna.shared.domain.valueobject.PageResult;
import ministere.sante.senpna.shared.domain.valueobject.Phone;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Tag("integration")
@Import({ OpportuniteCarriereRepositoryAdapter.class, OpportuniteCarriereMapper.class,
        CandidatureRepositoryAdapter.class, CandidatureMapper.class })
@DisplayName("Adaptateurs de persistence carriere — H2")
class CarrierePersistenceAdaptersTest {

    @Autowired
    OpportuniteCarriereRepositoryAdapter opportuniteAdapter;
    @Autowired
    CandidatureRepositoryAdapter candidatureAdapter;
    @Autowired
    OpportuniteCarriereJpaRepository opportuniteCarriereJpaRepository;
    @Autowired
    CandidatureJpaRepository candidatureJpaRepository;
    @Autowired
    TestEntityManager entityManager;

    OpportuniteCarriere ouverteDakar;
    OpportuniteCarriere brouillonThies;
    OpportuniteCarriere expireeMaisOuverte;

    @BeforeEach
    void setUp() {
        ouverteDakar = OpportuniteCarriere.creer(new OpportuniteCarriere.CreationCommand("Developpeur Java", "Entreprise X", "Desc", null, "Dakar",
                TypeContrat.CDI, LocalDate.now().plusMonths(2), LocalDate.now().plusMonths(1), UUID.randomUUID(),
                "Auteur", null));
        ouverteDakar.publier();

        brouillonThies = OpportuniteCarriere.creer(new OpportuniteCarriere.CreationCommand("Comptable", "Entreprise Y", "Desc", null, "Thies",
                TypeContrat.CDD, LocalDate.now().plusMonths(2), LocalDate.now().plusMonths(1), UUID.randomUUID(),
                "Auteur", null));

        expireeMaisOuverte = OpportuniteCarriere.builder()
            .id(ministere.sante.senpna.carriere.domain.valueobject.OpportuniteCarriereId.generate())
            .titre("Stage expire")
            .nomEntreprise("Entreprise Z")
            .description("Desc")
            .ficheDePosteUrl(null)
            .lieu("Dakar")
            .typeContrat(TypeContrat.STAGE)
            .dateDebut(LocalDate.now().minusDays(5))
            .dateLimiteCandidature(LocalDate.now().minusDays(1))
            .auteurId(UUID.randomUUID())
            .auteurNom("Auteur")
            .emailContact(null)
            .statut(StatutOpportunite.OUVERT)
            .createdAt(java.time.Instant.now())
            .updatedAt(java.time.Instant.now())
            .build();

        opportuniteAdapter.save(ouverteDakar);
        opportuniteAdapter.save(brouillonThies);
        opportuniteAdapter.save(expireeMaisOuverte);

        entityManager.flush();
        entityManager.clear();
    }

    @Nested
    @DisplayName("OpportuniteCarriereRepositoryAdapter.search()")
    class Search {

        @Test
        @DisplayName("filtre par texte sur le titre")
        void filtreParTexte() {
            PageResult<OpportuniteCarriere> result = opportuniteAdapter.search(
                    new OpportuniteCarriereSearchCriteria("comptable", null, null, false),
                    PageRequest.of(0, 20, null, null));

            assertThat(result.content()).extracting(OpportuniteCarriere::getTitre).containsExactly("Comptable");
        }

        @Test
        @DisplayName("publicOnly=true → seules les offres visibles publiquement et non expirées")
        void publicOnly_visiblesEtNonExpirees() {
            PageResult<OpportuniteCarriere> result = opportuniteAdapter.search(
                    new OpportuniteCarriereSearchCriteria(null, null, null, true), PageRequest.of(0, 20, null, null));

            assertThat(result.content()).extracting(OpportuniteCarriere::getTitre)
                    .containsExactly("Developpeur Java");
        }

        @Test
        @DisplayName("filtre par type de contrat")
        void filtreParTypeContrat() {
            PageResult<OpportuniteCarriere> result = opportuniteAdapter.search(
                    new OpportuniteCarriereSearchCriteria(null, TypeContrat.CDD, null, false),
                    PageRequest.of(0, 20, null, null));

            assertThat(result.content()).extracting(OpportuniteCarriere::getTitre).containsExactly("Comptable");
        }
    }

    @Nested
    @DisplayName("OpportuniteCarriereRepositoryAdapter.findOuvertesExpirees()")
    class FindOuvertesExpirees {

        @Test
        @DisplayName("ne renvoie que les offres OUVERT dont la date limite est dépassée")
        void neRenvoieQueLesOuvertesExpirees() {
            List<OpportuniteCarriere> result = opportuniteAdapter.findOuvertesExpirees(LocalDate.now());

            assertThat(result).extracting(OpportuniteCarriere::getTitre).containsExactly("Stage expire");
        }
    }

    @Nested
    @DisplayName("CandidatureRepositoryAdapter.search()")
    class SearchCandidatures {

        @Test
        @DisplayName("filtre par opportunité et texte")
        void filtreParOpportuniteEtTexte() {
            Candidature c1 = Candidature.soumettre(new Candidature.SoumissionCommand(ouverteDakar.getId().getValue(), Civilite.M, "Ibra Ndiaye",
                    Email.of("ibra@mail.sn"), Phone.of("+221771111111"), "cv", null, null, true, "T", "E", "rh@e.sn"));
            Candidature c2 = Candidature.soumettre(new Candidature.SoumissionCommand(brouillonThies.getId().getValue(), Civilite.MME, "Awa Fall",
                    Email.of("awa@mail.sn"), Phone.of("+221772222222"), "cv", null, null, true, "T", "E", "rh@e.sn"));
            candidatureAdapter.save(c1);
            candidatureAdapter.save(c2);
            entityManager.flush();
            entityManager.clear();

            PageResult<Candidature> result = candidatureAdapter.search(
                    new CandidatureSearchCriteria(ouverteDakar.getId().getValue(), null),
                    PageRequest.of(0, 20, null, null));

            assertThat(result.content()).extracting(Candidature::getNomComplet).containsExactly("Ibra Ndiaye");
        }
    }
}
