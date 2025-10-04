package my.app.quickline.controller;

import my.app.quickline.service.TwilioVerificationService;
import my.app.quickline.service.WhatsAppAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/verification")
@CrossOrigin(origins = "*")
public class VerificationController {

    private final TwilioVerificationService verificationService;
    private final WhatsAppAuthService whatsAppAuthService;

    @Autowired
    public VerificationController(
            TwilioVerificationService verificationService,
            WhatsAppAuthService whatsAppAuthService) {
        this.verificationService = verificationService;
        this.whatsAppAuthService = whatsAppAuthService;
    }

    @PostMapping("/send")
    public ResponseEntity<Map<String, Object>> sendVerificationCode(
            @RequestBody Map<String, String> request) {

        try {
            String phoneNumber = request.get("phoneNumber");

            if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "error", "Phone number is required"));
            }

            Map<String, Object> result = verificationService.sendVerificationCode(phoneNumber);

            if ((boolean) result.get("success")) {
                return ResponseEntity.ok(result);
            } else {
                return ResponseEntity.badRequest().body(result);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .body(Map.of("success", false, "error", "Internal server error: " + e.getMessage()));
        }
    }

    @PostMapping("/verify-and-login")
    public ResponseEntity<Map<String, Object>> verifyAndLogin(
            @RequestBody Map<String, String> request) {

        try {
            String phoneNumber = request.get("phoneNumber");
            String code = request.get("code");
            String name = request.get("name");

            System.out.println("📥 Verify and login request:");
            System.out.println("   Phone: " + phoneNumber);
            System.out.println("   Code: " + code);
            System.out.println("   Name: " + name);

            if (phoneNumber == null || code == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("verified", false, "error", "Phone number and code are required"));
            }

            // Проверяем код
            Map<String, Object> verifyResult = verificationService.verifyCode(phoneNumber, code);
            System.out.println("Verification result: " + verifyResult);

            if (!(boolean) verifyResult.get("verified")) {
                return ResponseEntity.badRequest().body(verifyResult);
            }

            // Код верный - авторизуем пользователя
            System.out.println("✅ Code verified, authenticating user...");
            Map<String, Object> authResult = whatsAppAuthService.authenticateByPhone(phoneNumber, name);

            System.out.println("✅ Authentication successful");

            return ResponseEntity.ok(Map.of(
                    "verified", true,
                    "message", "Phone verified and user authenticated",
                    "token", authResult.get("token"),
                    "user", authResult.get("user")
            ));
        } catch (Exception e) {
            System.err.println("❌ Error in verify-and-login:");
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .body(Map.of(
                            "verified", false,
                            "error", "Authentication failed: " + e.getMessage()
                    ));
        }
    }

    @PostMapping("/verify")
    public ResponseEntity<Map<String, Object>> verifyCode(
            @RequestBody Map<String, String> request) {

        try {
            String phoneNumber = request.get("phoneNumber");
            String code = request.get("code");

            if (phoneNumber == null || code == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("verified", false, "error", "Phone number and code are required"));
            }

            Map<String, Object> result = verificationService.verifyCode(phoneNumber, code);

            if ((boolean) result.get("verified")) {
                return ResponseEntity.ok(result);
            } else {
                return ResponseEntity.badRequest().body(result);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .body(Map.of("verified", false, "error", "Internal server error: " + e.getMessage()));
        }
    }

    // ✅ Правильно - путь будет /api/verification/status
    @GetMapping("/status")
    public ResponseEntity<?> getStatus() {
        return ResponseEntity.ok(Map.of("status", "Service is running"));
    }
}