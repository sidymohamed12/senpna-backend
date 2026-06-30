package ministere.sante.senpna.auth.infrastructure.otp;

import ministere.sante.senpna.auth.domain.valueobject.OtpChannel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class SmsOtpSenderAdapter implements ChannelOtpSender {

    private static final Logger log = LoggerFactory.getLogger(SmsOtpSenderAdapter.class);

    @Override
    public OtpChannel channel() {
        return OtpChannel.SMS;
    }

    @Override
    public void send(String destination, String code) {
        // TODO: brancher le client SMS définitif (Orange SMS API / Twilio / etc.)
        log.info("[OTP][SMS] (provider non configuré — log de secours) destination={}, code={}", destination, code);
    }
}
