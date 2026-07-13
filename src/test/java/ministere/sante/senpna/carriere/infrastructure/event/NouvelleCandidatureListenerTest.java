package ministere.sante.senpna.carriere.infrastructure.event;

import ministere.sante.senpna.carriere.domain.events.NouvelleCandidatureEvent;
import ministere.sante.senpna.carriere.domain.port.out.CandidatureMailPort;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("NouvelleCandidatureListener — envoi des e-mails liés à une nouvelle candidature")
class NouvelleCandidatureListenerTest {

    @Mock
    CandidatureMailPort candidatureMailPort;

    @Test
    @DisplayName("envoie l'accusé de réception au candidat ET la notification au contact RH")
    void envoieAccuseReceptionEtNotificationRH() {
        NouvelleCandidatureListener sut = new NouvelleCandidatureListener(candidatureMailPort);
        NouvelleCandidatureEvent event = new NouvelleCandidatureEvent(UUID.randomUUID(), UUID.randomUUID(),
                "Developpeur", "Entreprise X", "Ibra Ndiaye", "ibra@mail.sn", "+221771234567", "rh@entreprise.sn",
                Instant.now());

        sut.surNouvelleCandidature(event);

        verify(candidatureMailPort).envoyerAccuseReceptionCandidature("ibra@mail.sn", "Ibra Ndiaye", "Developpeur",
                "Entreprise X");
        verify(candidatureMailPort).envoyerNotificationNouvelleCandidature("rh@entreprise.sn", "Developpeur",
                "Entreprise X", "Ibra Ndiaye", "ibra@mail.sn", "+221771234567");
    }
}
