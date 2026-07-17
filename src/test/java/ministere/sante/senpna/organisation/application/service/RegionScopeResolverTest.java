package ministere.sante.senpna.organisation.application.service;

import ministere.sante.senpna.auth.fixtures.UserFixtures;
import ministere.sante.senpna.organisation.domain.exception.AccesRegionRefuseException;
import ministere.sante.senpna.organisation.domain.model.Entrepot;
import ministere.sante.senpna.organisation.domain.model.StructureSanitaire;
import ministere.sante.senpna.organisation.domain.port.out.EntrepotRepositoryPort;
import ministere.sante.senpna.organisation.domain.port.out.StructureSanitaireRepositoryPort;
import ministere.sante.senpna.organisation.domain.valueobject.EntrepotId;
import ministere.sante.senpna.organisation.domain.valueobject.RegionId;
import ministere.sante.senpna.organisation.domain.valueobject.StatutAdhesion;
import ministere.sante.senpna.organisation.domain.valueobject.StructureSanitaireId;
import ministere.sante.senpna.organisation.domain.valueobject.TypeEntrepot;
import ministere.sante.senpna.organisation.domain.valueobject.TypeStructureSanitaire;
import ministere.sante.senpna.shared.domain.model.User;
import ministere.sante.senpna.shared.domain.port.out.RoleCachePort;
import ministere.sante.senpna.shared.domain.port.out.UserAffectationRepositoryPort;
import ministere.sante.senpna.shared.domain.port.out.UserManagementRepositoryPort;
import ministere.sante.senpna.shared.domain.projection.RoleProjection;
import ministere.sante.senpna.shared.domain.projection.UserAffectationView;
import ministere.sante.senpna.shared.domain.valueobject.Email;
import ministere.sante.senpna.shared.domain.valueobject.HashedPassword;
import ministere.sante.senpna.shared.domain.valueobject.Nom;
import ministere.sante.senpna.shared.domain.valueobject.Prenom;
import ministere.sante.senpna.shared.domain.valueobject.UserId;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("RegionScopeResolver — portée régionale de gestion des entrepôts")
class RegionScopeResolverTest {

    @Mock
    UserManagementRepositoryPort userManagementRepositoryPort;
    @Mock
    RoleCachePort roleCachePort;
    @Mock
    UserAffectationRepositoryPort userAffectationRepositoryPort;
    @Mock
    EntrepotRepositoryPort entrepotRepositoryPort;
    @Mock
    StructureSanitaireRepositoryPort structureSanitaireRepositoryPort;

    RegionScopeResolver sut;

    private static final UUID ACTEUR_ID = UUID.randomUUID();
    private static final UUID ROLE_ADMIN_PNA_ID = UUID.fromString("e0000000-0000-0000-0000-000000000001");
    private static final UUID ROLE_ADMIN_PRA_ID = UUID.fromString("e0000000-0000-0000-0000-000000000002");

    @BeforeEach
    void setUp() {
        sut = new RegionScopeResolver(userManagementRepositoryPort, roleCachePort, userAffectationRepositoryPort,
                entrepotRepositoryPort, structureSanitaireRepositoryPort);
    }

    private User acteurAvecRoles(Set<UUID> roleIds) {
        return User.builder()
            .id(UserId.of(ACTEUR_ID))
            .nom(Nom.of(UserFixtures.NOM))
            .prenom(Prenom.of(UserFixtures.PRENOM))
            .email(Email.of(UserFixtures.EMAIL))
            .telephone(null)
            .hashedPassword(HashedPassword.of(UserFixtures.PASSWORD_HASH))
            .actif(true)
            .roleIds(roleIds)
            .tentativesEchecConnexion(0)
            .verrouilleJusqua(null)
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .build();
    }

    @Test
    @DisplayName("acteur national (ADMIN_PNA) → accès à toutes les régions, même affecté à une PRA")
    void verifierAccesRegion_acteurNational_toujoursAutorise() {
        when(userManagementRepositoryPort.findById(any()))
                .thenReturn(Optional.of(acteurAvecRoles(Set.of(ROLE_ADMIN_PNA_ID))));
        when(roleCachePort.findAllById(Set.of(ROLE_ADMIN_PNA_ID)))
                .thenReturn(Set.of(new RoleProjection(ROLE_ADMIN_PNA_ID, "ADMIN_PNA", "Administrateur PNA")));

        RegionId regionCible = RegionId.generate();

        assertThatCode(() -> sut.verifierAccesRegion(ACTEUR_ID, regionCible)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("acteur régional affecté à la même région → autorisé")
    void verifierAccesRegion_memeRegion_autorise() {
        RegionId regionCible = RegionId.generate();
        UUID entrepotId = UUID.randomUUID();

        when(userManagementRepositoryPort.findById(any()))
                .thenReturn(Optional.of(acteurAvecRoles(Set.of(ROLE_ADMIN_PRA_ID))));
        when(roleCachePort.findAllById(Set.of(ROLE_ADMIN_PRA_ID)))
                .thenReturn(Set.of(new RoleProjection(ROLE_ADMIN_PRA_ID, "ADMIN_PRA", "Administrateur PRA")));
        when(userAffectationRepositoryPort.findAffectation(ACTEUR_ID))
                .thenReturn(Optional.of(new UserAffectationView(ACTEUR_ID, entrepotId, null, null)));
        Entrepot entrepot = Entrepot.builder()
            .id(EntrepotId.of(entrepotId))
            .code("PRA-X")
            .nom("PRA X")
            .type(TypeEntrepot.PRA)
            .regionId(regionCible)
            .adresse(null)
            .telephone(null)
            .responsableUserId(null)
            .actif(true)
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .build();
        when(entrepotRepositoryPort.findById(EntrepotId.of(entrepotId))).thenReturn(Optional.of(entrepot));

        assertThatCode(() -> sut.verifierAccesRegion(ACTEUR_ID, regionCible)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("acteur régional affecté à une autre région → AccesRegionRefuseException")
    void verifierAccesRegion_autreRegion_refuse() {
        RegionId regionActeur = RegionId.generate();
        RegionId regionCible = RegionId.generate();
        UUID entrepotId = UUID.randomUUID();

        when(userManagementRepositoryPort.findById(any()))
                .thenReturn(Optional.of(acteurAvecRoles(Set.of(ROLE_ADMIN_PRA_ID))));
        when(roleCachePort.findAllById(Set.of(ROLE_ADMIN_PRA_ID)))
                .thenReturn(Set.of(new RoleProjection(ROLE_ADMIN_PRA_ID, "ADMIN_PRA", "Administrateur PRA")));
        when(userAffectationRepositoryPort.findAffectation(ACTEUR_ID))
                .thenReturn(Optional.of(new UserAffectationView(ACTEUR_ID, entrepotId, null, null)));
        Entrepot entrepot = Entrepot.builder()
            .id(EntrepotId.of(entrepotId))
            .code("PRA-X")
            .nom("PRA X")
            .type(TypeEntrepot.PRA)
            .regionId(regionActeur)
            .adresse(null)
            .telephone(null)
            .responsableUserId(null)
            .actif(true)
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .build();
        when(entrepotRepositoryPort.findById(EntrepotId.of(entrepotId))).thenReturn(Optional.of(entrepot));

        assertThatThrownBy(() -> sut.verifierAccesRegion(ACTEUR_ID, regionCible))
                .isInstanceOf(AccesRegionRefuseException.class);
    }

    @Test
    @DisplayName("acteur régional non affecté → aucune restriction (traité comme portée nationale)")
    void verifierAccesRegion_nonAffecte_autorise() {
        when(userManagementRepositoryPort.findById(any()))
                .thenReturn(Optional.of(acteurAvecRoles(Set.of(ROLE_ADMIN_PRA_ID))));
        when(roleCachePort.findAllById(Set.of(ROLE_ADMIN_PRA_ID)))
                .thenReturn(Set.of(new RoleProjection(ROLE_ADMIN_PRA_ID, "ADMIN_PRA", "Administrateur PRA")));
        when(userAffectationRepositoryPort.findAffectation(ACTEUR_ID)).thenReturn(Optional.empty());

        assertThatCode(() -> sut.verifierAccesRegion(ACTEUR_ID, RegionId.generate())).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("acteur régional affecté à la PNA centrale (sans région) → aucune restriction")
    void verifierAccesRegion_affecteAPnaCentrale_autorise() {
        UUID entrepotId = UUID.randomUUID();
        when(userManagementRepositoryPort.findById(any()))
                .thenReturn(Optional.of(acteurAvecRoles(Set.of(ROLE_ADMIN_PRA_ID))));
        when(roleCachePort.findAllById(Set.of(ROLE_ADMIN_PRA_ID)))
                .thenReturn(Set.of(new RoleProjection(ROLE_ADMIN_PRA_ID, "ADMIN_PRA", "Administrateur PRA")));
        when(userAffectationRepositoryPort.findAffectation(ACTEUR_ID))
                .thenReturn(Optional.of(new UserAffectationView(ACTEUR_ID, entrepotId, null, null)));
        Entrepot pnaCentral = Entrepot.builder()
            .id(EntrepotId.of(entrepotId))
            .code("PNA-CENTRAL")
            .nom("PNA Centrale")
            .type(TypeEntrepot.PNA_CENTRAL)
            .regionId(null)
            .adresse(null)
            .telephone(null)
            .responsableUserId(null)
            .actif(true)
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .build();
        when(entrepotRepositoryPort.findById(EntrepotId.of(entrepotId))).thenReturn(Optional.of(pnaCentral));

        assertThatCode(() -> sut.verifierAccesRegion(ACTEUR_ID, RegionId.generate())).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("acteur régional affecté à une structure sanitaire → région résolue depuis la structure")
    void resoudreRegionActeur_affecteAStructure_resoudRegionStructure() {
        RegionId regionStructure = RegionId.generate();
        UUID structureId = UUID.randomUUID();
        when(userManagementRepositoryPort.findById(any()))
                .thenReturn(Optional.of(acteurAvecRoles(Set.of(ROLE_ADMIN_PRA_ID))));
        when(roleCachePort.findAllById(Set.of(ROLE_ADMIN_PRA_ID)))
                .thenReturn(Set.of(new RoleProjection(ROLE_ADMIN_PRA_ID, "ADMIN_PRA", "Administrateur PRA")));
        when(userAffectationRepositoryPort.findAffectation(ACTEUR_ID))
                .thenReturn(Optional.of(new UserAffectationView(ACTEUR_ID, null, structureId, null)));
        StructureSanitaire structure = StructureSanitaire.builder()
            .id(StructureSanitaireId.of(structureId))
            .code("HOP-X")
            .nom("Hôpital X")
            .type(TypeStructureSanitaire.HOPITAL)
            .regionId(regionStructure)
            .praId(null)
            .district(null)
            .adresse(null)
            .telephone(null)
            .email(null)
            .responsableNom("Ndiaye")
            .responsablePrenom("Fatou")
            .statutAdhesion(StatutAdhesion.VALIDEE)
            .motifRejet(null)
            .actif(true)
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .build();
        when(structureSanitaireRepositoryPort.findById(StructureSanitaireId.of(structureId)))
                .thenReturn(Optional.of(structure));

        Optional<RegionId> result = sut.resoudreRegionActeur(ACTEUR_ID);

        assertThat(result).contains(regionStructure);
    }
}
