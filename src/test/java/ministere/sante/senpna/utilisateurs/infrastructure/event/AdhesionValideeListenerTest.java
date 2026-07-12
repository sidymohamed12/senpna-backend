package ministere.sante.senpna.utilisateurs.infrastructure.event;

import ministere.sante.senpna.shared.domain.events.AdhesionValideeEvent;
import ministere.sante.senpna.utilisateurs.domain.port.in.CreateGestionnaireStructureAccountUseCase;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("AdhesionValideeListener — consommation de l'événement d'adhésion validée")
class AdhesionValideeListenerTest {

    @Mock
    CreateGestionnaireStructureAccountUseCase createGestionnaireStructureAccountUseCase;

    @Test
    @DisplayName("délègue systématiquement au use case de création de compte")
    void delegueAuUseCase() {
        AdhesionValideeListener sut = new AdhesionValideeListener(createGestionnaireStructureAccountUseCase);
        AdhesionValideeEvent event = new AdhesionValideeEvent(UUID.randomUUID(), "Structure", "Ndiaye", "Fatou",
                "f@sante.gouv.sn", Instant.now());

        sut.surAdhesionValidee(event);

        verify(createGestionnaireStructureAccountUseCase).creer(event);
    }
}
