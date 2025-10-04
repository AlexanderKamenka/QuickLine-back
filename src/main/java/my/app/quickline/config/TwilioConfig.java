package my.app.quickline.config;

import com.twilio.Twilio;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;


@Configuration
public class TwilioConfig {

    @Value("${twilio.account.sid}")
    private String accountSid;

    @Value("${twilio.auth.token}")
    private String authToken;

    @Value("${twilio.dev.mode:true}")
    private boolean devMode;

    @PostConstruct
    public void init() {
        if (!devMode) {
            Twilio.init(accountSid, authToken);
            System.out.println("✅ Twilio initialized successfully");
        } else {
            System.out.println("⚠️ Twilio running in DEV MODE (no real messages)");
        }
    }
}
