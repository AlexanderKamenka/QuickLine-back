package my.app.quickline.service;

import my.app.quickline.model.entity.User;
import my.app.quickline.repository.UserRepository;
import my.app.quickline.security.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class WhatsAppAuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    /**
     * Авторизация/регистрация пользователя через номер телефона
     */
    public Map<String, Object> authenticateByPhone(String phoneNumber, String name) {
        try {
            String normalizedPhone = normalizePhoneNumber(phoneNumber);

            System.out.println("🔍 Authenticating phone: " + normalizedPhone);

            // Ищем или создаем пользователя
            User user = userRepository.findByPhoneNumber(normalizedPhone)
                    .orElseGet(() -> {
                        System.out.println("📝 Creating new user for: " + normalizedPhone);
                        return createNewUser(normalizedPhone, name);
                    });

            System.out.println("✅ User found/created: ID=" + user.getId() + ", Username=" + user.getUsername());

            // Генерируем JWT токен с дополнительными claims
            Map<String, Object> extraClaims = new HashMap<>();
            extraClaims.put("userId", user.getId());
            extraClaims.put("phoneNumber", user.getPhoneNumber());
            extraClaims.put("role", user.getRole().name());

            String token = jwtService.generateToken(extraClaims, user);

            System.out.println("✅ Token generated successfully");

            // Возвращаем ответ
            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("user", buildUserResponse(user));

            return response;
        } catch (Exception e) {
            System.err.println("❌ Error in authenticateByPhone: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Authentication failed: " + e.getMessage(), e);
        }
    }

    /**
     * Создание нового пользователя
     */
    private User createNewUser(String phoneNumber, String name) {
        try {
            String username = (name != null && !name.trim().isEmpty())
                    ? name.trim()
                    : generateUsername(phoneNumber);

            System.out.println("Creating user with username: " + username);

            User newUser = User.builder()
                    .phoneNumber(phoneNumber)
                    .username(username)
                    .role(User.Role.CLIENT)
                    .password(null)
                    .build();

            User savedUser = userRepository.save(newUser);
            System.out.println("✅ User saved with ID: " + savedUser.getId());

            return savedUser;
        } catch (Exception e) {
            System.err.println("❌ Error creating user: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * Построение ответа с данными пользователя
     */
    private Map<String, Object> buildUserResponse(User user) {
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("id", user.getId());
        userMap.put("username", user.getUsername());
        userMap.put("phoneNumber", user.getPhoneNumber());
        userMap.put("email", user.getEmail() != null ? user.getEmail() : "");
        userMap.put("role", user.getRole().name());
        return userMap;
    }

    /**
     * Генерация username из номера телефона
     */
    private String generateUsername(String phoneNumber) {
        String cleaned = phoneNumber.replaceAll("[^0-9]", "");
        String suffix = cleaned.substring(Math.max(0, cleaned.length() - 8));

        String baseUsername = "user_" + suffix;
        String username = baseUsername;
        int counter = 1;

        // Проверяем уникальность
        while (userRepository.findByUsername(username).isPresent()) {
            username = baseUsername + "_" + counter;
            counter++;
        }

        return username;
    }

    /**
     * Нормализация номера телефона
     */
    private String normalizePhoneNumber(String phoneNumber) {
        String normalized = phoneNumber.replaceAll("[\\s\\-\\(\\)]", "");
        if (!normalized.startsWith("+")) {
            normalized = "+" + normalized;
        }
        return normalized;
    }

    public boolean phoneNumberExists(String phoneNumber) {
        String normalized = normalizePhoneNumber(phoneNumber);
        return userRepository.existsByPhoneNumber(normalized);
    }

    public User getUserByPhoneNumber(String phoneNumber) {
        String normalized = normalizePhoneNumber(phoneNumber);
        return userRepository.findByPhoneNumber(normalized).orElse(null);
    }
}