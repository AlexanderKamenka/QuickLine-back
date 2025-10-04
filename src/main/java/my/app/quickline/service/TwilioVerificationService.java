package my.app.quickline.service;

import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TwilioVerificationService {

    @Value("${twilio.whatsapp.from}")
    private String twilioWhatsAppNumber;

    @Value("${twilio.dev.mode:true}")
    private boolean devMode;

    private static final int CODE_LENGTH = 6;
    private static final int CODE_EXPIRY_MINUTES = 5;
    private static final int MAX_ATTEMPTS = 3;
    private static final int RESEND_COOLDOWN_SECONDS = 60;

    private Map<String, VerificationCode> verificationCodes = new ConcurrentHashMap<>();

    /**
     * Внутренний класс для хранения кода верификации
     */
    private static class VerificationCode {
        String code;
        LocalDateTime expiryTime;
        LocalDateTime lastSentTime;
        int attempts;

        VerificationCode(String code) {
            this.code = code;
            this.expiryTime = LocalDateTime.now().plusMinutes(CODE_EXPIRY_MINUTES);
            this.lastSentTime = LocalDateTime.now();
            this.attempts = 0;
        }

        boolean isExpired() {
            return LocalDateTime.now().isAfter(expiryTime);
        }

        boolean canResend() {
            long secondsSinceLastSend = ChronoUnit.SECONDS.between(lastSentTime, LocalDateTime.now());
            return secondsSinceLastSend >= RESEND_COOLDOWN_SECONDS;
        }

        void updateLastSentTime() {
            this.lastSentTime = LocalDateTime.now();
        }

        void incrementAttempts() {
            this.attempts++;
        }

        boolean isTooManyAttempts() {
            return attempts >= MAX_ATTEMPTS;
        }

        long getRemainingSeconds() {
            return CODE_EXPIRY_MINUTES * 60 - ChronoUnit.SECONDS.between(
                    expiryTime.minusMinutes(CODE_EXPIRY_MINUTES),
                    LocalDateTime.now()
            );
        }
    }

    /**
     * Отправить код верификации через WhatsApp
     */
    public Map<String, Object> sendVerificationCode(String phoneNumber) {
        cleanExpiredCodes();

        // Нормализуем номер телефона
        String normalizedPhone = normalizePhoneNumber(phoneNumber);

        // Проверяем существующий код
        VerificationCode existingCode = verificationCodes.get(normalizedPhone);
        if (existingCode != null && !existingCode.isExpired()) {
            if (!existingCode.canResend()) {
                long waitTime = RESEND_COOLDOWN_SECONDS -
                        ChronoUnit.SECONDS.between(existingCode.lastSentTime, LocalDateTime.now());
                return Map.of(
                        "success", false,
                        "error", "Please wait " + waitTime + " seconds before requesting a new code"
                );
            }
        }

        // Генерируем новый код
        String code = generateCode();
        VerificationCode verificationCode = new VerificationCode(code);
        verificationCodes.put(normalizedPhone, verificationCode);

        // Формируем сообщение
        String message = String.format(
                "🔐 *Код верификации Beauty Salon*\n\n" +
                        "Ваш код: *%s*\n\n" +
                        "Код действителен %d минут.\n\n" +
                        "Если вы не запрашивали код, проигнорируйте это сообщение.",
                code, CODE_EXPIRY_MINUTES
        );

        // DEV MODE - вывод в консоль
        if (devMode) {
            printDevModeCode(normalizedPhone, code, verificationCode.getRemainingSeconds());
            return Map.of(
                    "success", true,
                    "message", "Code sent (DEV MODE - check console)",
                    "expiresIn", CODE_EXPIRY_MINUTES * 60
            );
        }

        // PRODUCTION MODE - реальная отправка через Twilio
        try {
            sendWhatsAppMessage(normalizedPhone, message);

            System.out.println("✅ WhatsApp code sent via Twilio to: " + normalizedPhone);

            return Map.of(
                    "success", true,
                    "message", "Verification code sent to WhatsApp",
                    "expiresIn", CODE_EXPIRY_MINUTES * 60
            );
        } catch (Exception e) {
            System.err.println("❌ Twilio error: " + e.getMessage());
            e.printStackTrace();

            // Fallback в консоль при ошибке
            printDevModeCode(normalizedPhone, code, verificationCode.getRemainingSeconds());

            return Map.of(
                    "success", false,
                    "error", "Failed to send WhatsApp message. Code printed to console (DEV FALLBACK)"
            );
        }
    }

    /**
     * Отправка сообщения через Twilio WhatsApp API
     */
    private void sendWhatsAppMessage(String phoneNumber, String messageText) {
        try {
            Message message = Message.creator(
                    new PhoneNumber("whatsapp:" + phoneNumber),
                    new PhoneNumber(twilioWhatsAppNumber),
                    messageText
            ).create();

            System.out.println("📤 Twilio Message SID: " + message.getSid());
            System.out.println("📊 Status: " + message.getStatus());
        } catch (Exception e) {
            System.err.println("❌ Twilio API Error: " + e.getMessage());
            throw new RuntimeException("Failed to send WhatsApp via Twilio: " + e.getMessage(), e);
        }
    }

    /**
     * Проверка введенного кода
     */
    public Map<String, Object> verifyCode(String phoneNumber, String code) {
        String normalizedPhone = normalizePhoneNumber(phoneNumber);
        VerificationCode storedCode = verificationCodes.get(normalizedPhone);

        if (storedCode == null) {
            System.out.println("❌ No code found for: " + normalizedPhone);
            return Map.of(
                    "verified", false,
                    "error", "No verification code found. Please request a new code."
            );
        }

        if (storedCode.isExpired()) {
            System.out.println("❌ Code expired for: " + normalizedPhone);
            verificationCodes.remove(normalizedPhone);
            return Map.of(
                    "verified", false,
                    "error", "Verification code has expired. Please request a new code."
            );
        }

        storedCode.incrementAttempts();

        if (storedCode.isTooManyAttempts()) {
            System.out.println("❌ Too many attempts for: " + normalizedPhone);
            verificationCodes.remove(normalizedPhone);
            return Map.of(
                    "verified", false,
                    "error", "Too many incorrect attempts. Please request a new code."
            );
        }

        if (storedCode.code.equals(code)) {
            System.out.println("✅ Code verified successfully for: " + normalizedPhone);
            verificationCodes.remove(normalizedPhone);
            return Map.of(
                    "verified", true,
                    "message", "Phone number verified successfully"
            );
        }

        int remainingAttempts = MAX_ATTEMPTS - storedCode.attempts;
        System.out.println("❌ Invalid code for: " + normalizedPhone +
                " (attempts: " + storedCode.attempts + "/" + MAX_ATTEMPTS + ")");

        return Map.of(
                "verified", false,
                "error", "Invalid verification code. " + remainingAttempts + " attempts remaining."
        );
    }

    /**
     * Нормализация номера телефона
     */
    private String normalizePhoneNumber(String phoneNumber) {
        // Убираем пробелы, дефисы, скобки
        String normalized = phoneNumber.replaceAll("[\\s\\-\\(\\)]", "");

        // Если не начинается с +, добавляем
        if (!normalized.startsWith("+")) {
            normalized = "+" + normalized;
        }

        return normalized;
    }

    /**
     * Генерация случайного 6-значного кода
     */
    private String generateCode() {
        SecureRandom random = new SecureRandom();
        int code = random.nextInt(999999);
        return String.format("%06d", code);
    }

    /**
     * Очистка истекших кодов
     */
    private void cleanExpiredCodes() {
        verificationCodes.entrySet().removeIf(entry -> entry.getValue().isExpired());
    }

    /**
     * Вывод кода в консоль для DEV MODE
     */
    private void printDevModeCode(String phone, String code, long expiresInSeconds) {
        System.out.println("\n╔═══════════════════════════════════════════════════╗");
        System.out.println("║        📱 VERIFICATION CODE (DEV MODE)            ║");
        System.out.println("╠═══════════════════════════════════════════════════╣");
        System.out.println("║  Phone:    " + String.format("%-37s", phone) + "║");
        System.out.println("║  Code:     " + String.format("%-37s", code) + "║");
        System.out.println("║  Expires:  " + String.format("%-37s", expiresInSeconds + " seconds") + "║");
        System.out.println("╚═══════════════════════════════════════════════════╝\n");
    }

    /**
     * Получить статистику для мониторинга
     */
    public Map<String, Object> getStats() {
        cleanExpiredCodes();
        return Map.of(
                "service", "Twilio WhatsApp Verification",
                "devMode", devMode,
                "activeCodes", verificationCodes.size(),
                "codeExpiryMinutes", CODE_EXPIRY_MINUTES,
                "maxAttempts", MAX_ATTEMPTS
        );
    }
}