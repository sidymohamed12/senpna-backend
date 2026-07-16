package ministere.sante.senpna.auth.infrastructure.persistence.adapter;

import ministere.sante.senpna.auth.infrastructure.persistence.entity.RoleJpaEntity;
import ministere.sante.senpna.auth.infrastructure.persistence.repository.RoleJpaRepository;
import ministere.sante.senpna.shared.domain.exception.ErrorCategory;
import ministere.sante.senpna.shared.domain.exception.SenPnaException;
import ministere.sante.senpna.shared.domain.projection.RoleProjection;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("RoleQueryAdapter")
class RoleQueryAdapterTest {

    @Mock
    RoleJpaRepository roleJpaRepository;

    RoleQueryAdapter sut;

    @BeforeEach
    void setUp() {
        sut = new RoleQueryAdapter(roleJpaRepository);
    }

    @Test
    @DisplayName("findAll() mappe chaque entité en projection")
    void findAll_mappeChaqueEntite() {
        RoleJpaEntity entity = new RoleJpaEntity(UUID.randomUUID(), "ADMIN_PNA", "Administrateur PNA");
        when(roleJpaRepository.findAll()).thenReturn(List.of(entity));

        List<RoleProjection> result = sut.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).code()).isEqualTo("ADMIN_PNA");
        assertThat(result.get(0).nom()).isEqualTo("Administrateur PNA");
    }

    @Test
    @DisplayName("existsById() délègue au repository")
    void existsById_delegue() {
        UUID id = UUID.randomUUID();
        when(roleJpaRepository.existsById(id)).thenReturn(true);

        assertThat(sut.existsById(id)).isTrue();
    }

    @Test
    @DisplayName("findNomById() retourne le nom quand le rôle existe")
    void findNomById_roleExiste() {
        UUID id = UUID.randomUUID();
        when(roleJpaRepository.findNomById(id)).thenReturn(Optional.of("Administrateur PNA"));

        assertThat(sut.findNomById(id)).isEqualTo("Administrateur PNA");
    }

    @Test
    @DisplayName("findNomById() rôle introuvable → SenPnaException (catégorie NOT_FOUND)")
    void findNomById_roleIntrouvable_leveException() {
        UUID id = UUID.randomUUID();
        when(roleJpaRepository.findNomById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sut.findNomById(id))
                .isInstanceOf(SenPnaException.class)
                .satisfies(ex -> assertThat(((SenPnaException) ex).getCategory()).isEqualTo(ErrorCategory.NOT_FOUND));
    }

    @Test
    @DisplayName("findByCode() présent → projection ; absent → vide")
    void findByCode() {
        when(roleJpaRepository.findByCode("ADMIN_PNA"))
                .thenReturn(Optional.of(new RoleJpaEntity(UUID.randomUUID(), "ADMIN_PNA", "Administrateur PNA")));
        when(roleJpaRepository.findByCode("INCONNU")).thenReturn(Optional.empty());

        assertThat(sut.findByCode("ADMIN_PNA")).isPresent();
        assertThat(sut.findByCode("INCONNU")).isEmpty();
    }

    @Test
    @DisplayName("findById() présent → projection ; absent → vide")
    void findById() {
        UUID id = UUID.randomUUID();
        when(roleJpaRepository.findById(id))
                .thenReturn(Optional.of(new RoleJpaEntity(UUID.randomUUID(), "ADMIN_PNA", "Administrateur PNA")));

        assertThat(sut.findById(id)).isPresent();
        assertThat(sut.findById(UUID.randomUUID())).isEmpty();
    }
}