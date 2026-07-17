package ministere.sante.senpna.appeloffre.application.usecase;

import ministere.sante.senpna.appeloffre.domain.model.AppelOffre;
import ministere.sante.senpna.appeloffre.domain.model.LigneAppelOffre;
import ministere.sante.senpna.appeloffre.domain.port.out.AppelOffreRepositoryPort;
import ministere.sante.senpna.appeloffre.domain.valueobject.AppelOffreId;
import ministere.sante.senpna.appeloffre.domain.valueobject.StatutAppelOffre;
import ministere.sante.senpna.medicament.domain.valueobject.MedicamentId;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ClorerAppelOffresExpiresUseCaseImpl")
class ClorerAppelOffresExpiresUseCaseImplTest {

    @Mock
    AppelOffreRepositoryPort appelOffreRepositoryPort;

    ClorerAppelOffresExpiresUseCaseImpl sut;

    @BeforeEach
    void setUp() {
        sut = new ClorerAppelOffresExpiresUseCaseImpl(appelOffreRepositoryPort);
    }

    private AppelOffre appelOffrePublieExpire() {
        LigneAppelOffre ligne = LigneAppelOffre.creer(MedicamentId.generate(), "Med", BigDecimal.TEN, "u");
        Instant maintenant = Instant.now();
        return AppelOffre.builder()
            .id(AppelOffreId.generate())
            .reference("AO-1")
            .objet("Objet")
            .dateCloture(LocalDate.now().minusDays(1))
            .statut(StatutAppelOffre.PUBLIE)
            .lignes(List.of(ligne))
            .createdAt(maintenant)
            .updatedAt(maintenant)
            .build();
    }

    @Test
    @DisplayName("aucun AO expiré → ne sauvegarde rien, retourne 0")
    void aucunExpire_neSauvegardeRien() {
        when(appelOffreRepositoryPort.findPubliesAvecClotureDepassee()).thenReturn(List.of());

        int resultat = sut.clorerExpires();

        assertThat(resultat).isZero();
        verify(appelOffreRepositoryPort, never()).save(any());
    }

    @Test
    @DisplayName("clôture chaque AO expiré trouvé et retourne leur nombre")
    void clotureChaqueAoExpire() {
        AppelOffre ao1 = appelOffrePublieExpire();
        AppelOffre ao2 = appelOffrePublieExpire();
        when(appelOffreRepositoryPort.findPubliesAvecClotureDepassee()).thenReturn(List.of(ao1, ao2));
        when(appelOffreRepositoryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));

        int resultat = sut.clorerExpires();

        assertThat(resultat).isEqualTo(2);
        assertThat(ao1.getStatut()).isEqualTo(StatutAppelOffre.CLOTURE);
        assertThat(ao2.getStatut()).isEqualTo(StatutAppelOffre.CLOTURE);
        verify(appelOffreRepositoryPort, times(2)).save(any());
    }
}
