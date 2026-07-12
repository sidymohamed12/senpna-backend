package ministere.sante.senpna.utilisateurs.application.usecase;

import ministere.sante.senpna.auth.fixtures.UserFixtures;
import ministere.sante.senpna.shared.domain.criteria.UserSearchCriteria;
import ministere.sante.senpna.shared.domain.model.User;
import ministere.sante.senpna.shared.domain.port.out.UserManagementRepositoryPort;
import ministere.sante.senpna.shared.domain.valueobject.PageRequest;
import ministere.sante.senpna.shared.domain.valueobject.PageResult;
import ministere.sante.senpna.utilisateurs.application.service.UserDetailAssembler;
import ministere.sante.senpna.utilisateurs.domain.command.UserCommands.ListUsersQuery;
import ministere.sante.senpna.utilisateurs.domain.command.UserCommands.UserDetail;
import ministere.sante.senpna.utilisateurs.domain.command.UserCommands.UserPage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ListUsersUseCaseImpl — recherche paginée des utilisateurs")
class ListUsersUseCaseImplTest {

    @Mock
    UserManagementRepositoryPort userManagementRepositoryPort;
    @Mock
    UserDetailAssembler userDetailAssembler;

    ListUsersUseCaseImpl sut;

    @BeforeEach
    void setUp() {
        sut = new ListUsersUseCaseImpl(userManagementRepositoryPort, userDetailAssembler);
    }

    @Test
    @DisplayName("construit les critères de recherche à partir de la query et mappe chaque résultat")
    void construitCriteresEtMappeResultats() {
        User user = UserFixtures.actif();
        PageResult<User> pageResult = PageResult.of(List.of(user), 0, 20, 1);
        when(userManagementRepositoryPort.search(eq(new UserSearchCriteria("dia", true, null)), any()))
                .thenReturn(pageResult);
        UserDetail detail = new UserDetail(UserFixtures.USER_ID, "N", "P", "e", null, true, java.util.Set.of(),
                null, null, null, null);
        when(userDetailAssembler.assembler(user)).thenReturn(detail);

        UserPage page = sut.lister(new ListUsersQuery("dia", true, null, 0, 20, null, null));

        assertThat(page.content()).containsExactly(detail);
        assertThat(page.totalElements()).isEqualTo(1);
    }

    @Test
    @DisplayName("aucun résultat → page de contenu vide, sans erreur")
    void aucunResultat_pageVide() {
        when(userManagementRepositoryPort.search(any(), any()))
                .thenReturn(PageResult.of(List.of(), 0, 20, 0));

        UserPage page = sut.lister(new ListUsersQuery(null, null, null, null, null, null, null));

        assertThat(page.content()).isEmpty();
        assertThat(page.totalElements()).isZero();
    }

    @Test
    @DisplayName("délègue à PageRequest.of() pour construire les paramètres de pagination")
    void delegueConstructionPageRequest() {
        when(userManagementRepositoryPort.search(any(), eq(PageRequest.of(2, 10, "nom", "ASC"))))
                .thenReturn(PageResult.of(List.of(), 2, 10, 0));

        UserPage page = sut.lister(new ListUsersQuery(null, null, null, 2, 10, "nom", "ASC"));

        assertThat(page.page()).isEqualTo(2);
        assertThat(page.size()).isEqualTo(10);
    }
}
