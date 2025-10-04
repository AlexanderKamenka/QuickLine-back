package my.app.quickline.security;

import lombok.RequiredArgsConstructor;
import my.app.quickline.model.entity.User;
import my.app.quickline.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        System.out.println("🔍 Loading user: " + username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден: " + username));

        System.out.println("✅ User found: ID=" + user.getId() + ", Role=" + user.getRole());
        System.out.println("   Password: " + (user.getPassword() != null ? "SET" : "NULL"));

        return user;
    }
}