package dev.nastiausenko.movies.user;

import dev.nastiausenko.movies.jwt.JwtUtil;
import dev.nastiausenko.movies.review.exception.ForbiddenException;
import dev.nastiausenko.movies.user.exception.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
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
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    public String registerUser(User request) {
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
        return loginUser(request);
    }

    public String loginUser(User request) {
        try {
            if (userRepository.findByEmail(request.getEmail()).isEmpty()) {
                throw new UserNotFoundException();
            }
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);

            return jwtUtil.generateToken(authentication);
        } catch (BadCredentialsException e) {
            throw new BadCredentialsException("Invalid credentials");
        }
    }

    public void editUsername(String username) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String userName = auth.getName();
            User user = userRepository.findByUsername(userName).orElseThrow(UserNotFoundException::new);
            Optional<User> existingUser = userRepository.findByUsername(username);
            if (existingUser.isPresent()) {
                throw new UsernameAlreadyTakenException(username);
            }

            user.setUsername(username);
            userRepository.save(user);
        } catch (Exception e) {
            throw new ForbiddenException("You are not allowed to edit username");
        }
    }

    public void editPassword(String password) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String userName = auth.getName();
            User user = userRepository.findByUsername(userName).orElseThrow(UserNotFoundException::new);
            if (passwordEncoder.matches(password, user.getPassword())) {
                throw new SameNewPasswordException();
            }

            user.setPassword(passwordEncoder.encode(password));
            userRepository.save(user);
        } catch (Exception e) {
            throw new ForbiddenException("You are not allowed to edit password");
        }
    }
}
