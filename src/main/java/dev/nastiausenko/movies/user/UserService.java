package dev.nastiausenko.movies.user;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public User registerUser(String username, String email, String password) {
        // Перевірка наявності користувача з таким самим ім'ям або email
        Optional<User> existingUser = userRepository.findByUsername(username);
        if (existingUser.isPresent()) {
            throw new RuntimeException("User with this username already exists");
        }

        // Створення нового користувача
        User user = User.builder()
                .username(username)
                .email(email)
                .password(passwordEncoder.encode(password)) // Захист пароля
                .build();

        // Збереження користувача в MongoDB
        return userRepository.save(user);
    }
}
