package ministere.sante.senpna.auth.infrastructure.otp;

import ministere.sante.senpna.auth.domain.valueobject.OtpChannel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class EmailOtpSenderAdapter implements ChannelOtpSender {

    private static final Logger log = LoggerFactory.getLogger(EmailOtpSenderAdapter.class);

    @Override
    public OtpChannel channel() {
        return OtpChannel.EMAIL;
    }

    @Override
    public void send(String destination, String code) {
        // TODO: brancher le client e-mail définitif (JavaMailSender / SendGrid / etc.)
        log.info("[OTP][EMAIL] (provider non configuré — log de secours) destination={}, code={}", destination,
                code);
    }
}
