package ministere.sante.senpna.auth.infrastructure.otp;

import ministere.sante.senpna.auth.domain.port.out.OtpSenderPort;
import ministere.sante.senpna.auth.domain.valueobject.OtpChannel;
import ministere.sante.senpna.shared.domain.exception.BusinessRuleException;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class CompositeOtpSenderAdapter implements OtpSenderPort {

    private final Map<OtpChannel, ChannelOtpSender> sendersByChannel;

    public CompositeOtpSenderAdapter(List<ChannelOtpSender> senders) {
        this.sendersByChannel = senders.stream()
                .collect(Collectors.toMap(ChannelOtpSender::channel, Function.identity()));
    }

    @Override
    public void send(OtpChannel channel, String destination, String code) {
        ChannelOtpSender sender = sendersByChannel.get(channel);
        if (sender == null) {
            throw new BusinessRuleException("Canal OTP non supporté : " + channel, "OTP_CHANNEL_NOT_SUPPORTED");
        }
        sender.send(destination, code);
    }
}
