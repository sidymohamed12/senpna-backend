package ministere.sante.senpna.auth.infrastructure.otp;

import ministere.sante.senpna.auth.domain.valueobject.OtpChannel;
import ministere.sante.senpna.shared.infrastructure.exception.BusinessRuleException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CompositeOtpSenderAdapter")
class CompositeOtpSenderAdapterTest {

    @Mock private ChannelOtpSender emailSender;
    @Mock private ChannelOtpSender smsSender;

    private CompositeOtpSenderAdapter sut;

    @BeforeEach
    void setup() {
        when(emailSender.channel()).thenReturn(OtpChannel.EMAIL);
        when(smsSender.channel()).thenReturn(OtpChannel.SMS);
        sut = new CompositeOtpSenderAdapter(List.of(emailSender, smsSender));
        // channel() est appelé pendant la construction du composite (Collectors.toMap).
        // On efface ces interactions pour que verifyNoInteractions() ne les voie pas.
        clearInvocations(emailSender, smsSender);
    }

    @Test
    @DisplayName("canal EMAIL → route vers le sender EMAIL")
    void send_email_route_vers_email_sender() {
        sut.send(OtpChannel.EMAIL, "user@example.com", "123456");

        verify(emailSender).send("user@example.com", "123456");
        verifyNoInteractions(smsSender);
    }

    @Test
    @DisplayName("canal SMS → route vers le sender SMS")
    void send_sms_route_vers_sms_sender() {
        sut.send(OtpChannel.SMS, "+221771234567", "654321");

        verify(smsSender).send("+221771234567", "654321");
        verifyNoInteractions(emailSender);
    }

    @Test
    @DisplayName("canal non supporté → BusinessRuleException avec code OTP_CHANNEL_NOT_SUPPORTED")
    void send_canal_non_supporte_leve_exception() {
        // On crée un composite qui ne connaît aucun sender pour simuler un canal inconnu
        CompositeOtpSenderAdapter sutSansEmail = new CompositeOtpSenderAdapter(List.of(smsSender));

        assertThatThrownBy(() -> sutSansEmail.send(OtpChannel.EMAIL, "dest", "code"))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("EMAIL");
    }

    @Test
    @DisplayName("liste vide de senders → BusinessRuleException pour tout canal")
    void send_aucun_sender_leve_exception() {
        CompositeOtpSenderAdapter sutVide = new CompositeOtpSenderAdapter(List.of());

        assertThatThrownBy(() -> sutVide.send(OtpChannel.EMAIL, "dest", "code"))
                .isInstanceOf(BusinessRuleException.class);
        assertThatThrownBy(() -> sutVide.send(OtpChannel.SMS, "dest", "code"))
                .isInstanceOf(BusinessRuleException.class);
    }
}
