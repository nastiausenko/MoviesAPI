package dev.nastiausenko.movies.user;

import dev.nastiausenko.movies.category.Category;
import dev.nastiausenko.movies.config.jwt.JwtUtil;
import dev.nastiausenko.movies.user.exception.EmailAlreadyTakenException;
import dev.nastiausenko.movies.user.exception.SameNewPasswordException;
import dev.nastiausenko.movies.user.exception.UserNotFoundException;
import dev.nastiausenko.movies.user.exception.UsernameAlreadyTakenException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    public String registerUser(String username, String email, String password) {
        Optional<User> existingUser = userRepository.findByName(username);
        Optional<User> existingEmail = userRepository.findByEmail(email);

        if (existingUser.isPresent()) {
            throw new UsernameAlreadyTakenException(username);
        }

        if (existingEmail.isPresent()) {
            throw new EmailAlreadyTakenException(email);
        }

        User user = User.builder()
                .name(username)
                .email(email)
                .password(passwordEncoder.encode(password))
                .roles(Set.of("USER"))
                .isBlocked(false)
                .build();

        userRepository.save(user);
        return loginUser(email, password);
    }

    public String loginUser(String email, String password) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password)
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);

            return jwtUtil.generateToken(authentication);
        } catch (BadCredentialsException e) {
            throw new BadCredentialsException("Invalid credentials");
        }
    }

    public void editUsername(String username) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userName = auth.getName();
        User user = userRepository.findByName(userName).orElseThrow(UserNotFoundException::new);
        Optional<User> existingUser = userRepository.findByName(username);

        if (existingUser.isPresent()) {
            throw new UsernameAlreadyTakenException(username);
        }

        user.setName(username);
        userRepository.save(user);
    }

    public void editPassword(String password) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userName = auth.getName();
        User user = userRepository.findByName(userName).orElseThrow(UserNotFoundException::new);
        if (passwordEncoder.matches(password, user.getPassword())) {
            throw new SameNewPasswordException();
        }

        user.setPassword(passwordEncoder.encode(password));
        userRepository.save(user);
    }

    public List<Category> getCategories() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userName = auth.getName();
        User user = userRepository.findByName(userName).orElseThrow(UserNotFoundException::new);

        return user.getCategories();
    }

    public void deleteAccount() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        userRepository.deleteByEmail(auth.getName());

    }
}
