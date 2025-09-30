package my.app.quickline.service;

import lombok.RequiredArgsConstructor;

import my.app.quickline.exception.BadRequestException;
import my.app.quickline.model.dto.AuthRequest;
import my.app.quickline.model.dto.AuthResponse;
import my.app.quickline.model.dto.RegisterRequest;
import my.app.quickline.model.entity.User;
import my.app.quickline.repository.UserRepository;
import my.app.quickline.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthResponse register(RegisterRequest request) {
        // Проверяем, что пользователь с таким именем ещё не существует
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new BadRequestException("Пользователь с таким именем уже существует");
        }

        // Проверяем, что пользователь с таким email ещё не существует
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new BadRequestException("Пользователь с таким email уже существует");
        }

        // Создаем нового пользователя
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .build();

        userRepository.save(user);

        // Генерируем JWT токен
        String token = jwtService.generateToken(user);

        // Возвращаем ответ с токеном и данными пользователя
        return AuthResponse.builder()
                .token(token)
                .userId(user.getId())
                .username(user.getUsername())
                .build();
    }

    public AuthResponse login(AuthRequest request) {
        // Аутентификация пользователя
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        // Получаем пользователя
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BadRequestException("Пользователь не найден"));

        // Генерируем JWT токен
        String token = jwtService.generateToken(user);

        // Возвращаем ответ с токеном и данными пользователя
        return AuthResponse.builder()
                .token(token)
                .userId(user.getId())
                .username(user.getUsername())
                .build();
    }
}