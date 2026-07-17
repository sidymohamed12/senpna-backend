package ministere.sante.senpna.carriere.application.usecase;

import ministere.sante.senpna.carriere.application.service.CandidatureDetailAssembler;
import ministere.sante.senpna.carriere.domain.command.CandidatureCommands.CandidatureDetail;
import ministere.sante.senpna.carriere.domain.command.CandidatureCommands.CandidaturePage;
import ministere.sante.senpna.carriere.domain.command.CandidatureCommands.GetCandidatureQuery;
import ministere.sante.senpna.carriere.domain.command.CandidatureCommands.ListCandidaturesQuery;
import ministere.sante.senpna.carriere.domain.exception.CandidatureIntrouvableException;
import ministere.sante.senpna.carriere.domain.model.Candidature;
import ministere.sante.senpna.carriere.domain.port.out.CandidatureRepositoryPort;
import ministere.sante.senpna.carriere.domain.valueobject.Civilite;
import ministere.sante.senpna.shared.domain.valueobject.Email;
import ministere.sante.senpna.shared.domain.valueobject.PageResult;
import ministere.sante.senpna.shared.domain.valueobject.Phone;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetCandidatureUseCaseImpl / ListCandidaturesUseCaseImpl")
class GetCandidatureUseCaseImplTest {

    @Mock
    CandidatureRepositoryPort candidatureRepositoryPort;
    @Mock
    CandidatureDetailAssembler assembler;

    @Nested
    @DisplayName("GetCandidatureUseCaseImpl")
    class Get {

        GetCandidatureUseCaseImpl sut;

        @BeforeEach
        void setUp() {
            sut = new GetCandidatureUseCaseImpl(candidatureRepositoryPort, assembler);
        }

        @Test
        @DisplayName("candidature trouvée → renvoie le détail assemblé")
        void trouvee_renvoieDetail() {
            UUID id = UUID.randomUUID();
            Candidature candidature = Candidature.soumettre(new Candidature.SoumissionCommand(UUID.randomUUID(), Civilite.M, "Ibra Ndiaye",
                    Email.of("ibra@mail.sn"), Phone.of("+221771234567"), "cv", null, null, true, "T", "E", "e@e.sn"));
            when(candidatureRepositoryPort.findById(any())).thenReturn(Optional.of(candidature));
            CandidatureDetail detail = new CandidatureDetail(id, null, "M", "Ibra Ndiaye", "ibra@mail.sn",
                    "+221771234567", "cv", null, null, true, null);
            when(assembler.assembler(candidature)).thenReturn(detail);

            assertThat(sut.obtenir(new GetCandidatureQuery(id))).isSameAs(detail);
        }

        @Test
        @DisplayName("introuvable → CandidatureIntrouvableException")
        void introuvable_leveException() {
            when(candidatureRepositoryPort.findById(any())).thenReturn(Optional.empty());

            var getCandidatureQuery = new GetCandidatureQuery(UUID.randomUUID());
            assertThatThrownBy(() -> sut.obtenir(getCandidatureQuery))
                    .isInstanceOf(CandidatureIntrouvableException.class);
        }
    }

    @Nested
    @DisplayName("ListCandidaturesUseCaseImpl")
    class List_ {

        ListCandidaturesUseCaseImpl sut;

        @BeforeEach
        void setUp() {
            sut = new ListCandidaturesUseCaseImpl(candidatureRepositoryPort, assembler);
        }

        @Test
        @DisplayName("aucun résultat → page de contenu vide")
        void aucunResultat_pageVide() {
            when(candidatureRepositoryPort.search(any(), any())).thenReturn(PageResult.of(List.of(), 0, 20, 0));

            CandidaturePage page = sut.lister(
                    new ListCandidaturesQuery(null, null, null, null, null, null));

            assertThat(page.content()).isEmpty();
        }
    }
}
