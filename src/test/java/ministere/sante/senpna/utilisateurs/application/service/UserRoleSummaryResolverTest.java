package ministere.sante.senpna.utilisateurs.application.service;

import ministere.sante.senpna.shared.domain.port.out.RoleCachePort;
import ministere.sante.senpna.shared.domain.projection.RoleProjection;
import ministere.sante.senpna.utilisateurs.domain.command.UserCommands.RoleSummary;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserRoleSummaryResolver — résolution de résumés de rôle")
class UserRoleSummaryResolverTest {

    @Mock
    RoleCachePort roleCachePort;

    UserRoleSummaryResolver sut;

    @BeforeEach
    void setUp() {
        sut = new UserRoleSummaryResolver(roleCachePort);
    }

    @Test
    @DisplayName("convertit chaque RoleProjection résolue en RoleSummary")
    void convertitChaqueProjection() {
        UUID roleId = UUID.randomUUID();
        when(roleCachePort.findAllById(Set.of(roleId))).thenReturn(
                Set.of(new RoleProjection(roleId, "ADMIN_PNA", "Administrateur national")));

        Set<RoleSummary> result = sut.resoudre(Set.of(roleId));

        assertThat(result).containsExactly(new RoleSummary(roleId, "ADMIN_PNA", "Administrateur national"));
    }

    @Test
    @DisplayName("identifiant sans correspondance en cache → simplement absent du résultat")
    void identifiantInconnu_absent() {
        UUID roleId = UUID.randomUUID();
        when(roleCachePort.findAllById(Set.of(roleId))).thenReturn(Set.of());

        assertThat(sut.resoudre(Set.of(roleId))).isEmpty();
    }

    @Test
    @DisplayName("ensemble vide → résultat vide")
    void ensembleVide() {
        when(roleCachePort.findAllById(Set.of())).thenReturn(Set.of());

        assertThat(sut.resoudre(Set.of())).isEmpty();
    }
}
