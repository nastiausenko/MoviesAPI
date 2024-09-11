package dev.nastiausenko.movies.user;

import dev.nastiausenko.movies.user.exception.EmailAlreadyTakenException;
import dev.nastiausenko.movies.user.exception.SameNewPasswordException;
import dev.nastiausenko.movies.user.exception.UserNotFoundException;
import dev.nastiausenko.movies.user.exception.UsernameAlreadyTakenException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public void registerUser(User request) {
        Optional<User> existingUser = userRepository.findByUsername(request.getUsername());
        Optional<User> existingEmail = userRepository.findByEmail(request.getEmail());

        if (existingUser.isPresent()) {
            throw new UsernameAlreadyTakenException(request.getUsername());
        }

        if (existingEmail.isPresent()) {
            throw new EmailAlreadyTakenException(request.getEmail());
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword())) // Захист пароля
                .build();

        userRepository.save(user);
    }

    public void editUsername(String username) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userName = auth.getName();
        User user = userRepository.findByUsername(userName).orElseThrow(UserNotFoundException::new);
        Optional<User> existingUser = userRepository.findByUsername(username);
        if (existingUser.isPresent()) {
            throw new UsernameAlreadyTakenException(username);
        }

        user.setUsername(username);
        userRepository.save(user);
    }

    public void editPassword(String password) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userName = auth.getName();
        User user = userRepository.findByUsername(userName).orElseThrow(UserNotFoundException::new);
        if (passwordEncoder.matches(password, user.getPassword())) {
            throw new SameNewPasswordException();
        }

        user.setPassword(passwordEncoder.encode(password));
        userRepository.save(user);
    }

    public User findUserByUsername(String username) {
        return userRepository.findByUsername(username).orElseThrow(UserNotFoundException::new);
    }
}
