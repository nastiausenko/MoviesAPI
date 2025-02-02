package dev.nastiausenko.movies;

import dev.nastiausenko.movies.config.jwt.JwtUtil;
import dev.nastiausenko.movies.user.UserService;
import dev.nastiausenko.movies.user.User;
import dev.nastiausenko.movies.user.UserRepository;
import dev.nastiausenko.movies.user.exception.EmailAlreadyTakenException;
import dev.nastiausenko.movies.user.exception.UsernameAlreadyTakenException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
public class UserServiceTests {

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserService userService;

    private User user;

    @BeforeEach
    public void setUp() {
        user = User.builder()
                .username("Username1")
                .email("example@email.com")
                .password("Password07")
                .roles(Set.of("USER"))
                .isBlocked(false)
                .build();
    }

    @Test
    void shouldRegisterUserSuccessfully() {
        when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(user.getPassword())).thenReturn("encodedPassword");
        when(jwtUtil.generateToken(any())).thenReturn("JWT");

        String token = userService.registerUser(user.getUsername(), user.getEmail(), user.getPassword());

        assertNotNull(token);
        verify(userRepository).save(any(User.class));
        verify(userRepository).findByEmail(user.getEmail());
    }

    @Test
    void shouldThrowExceptionWhenEmailIsAlreadyTaken() {
       when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
       assertThrows(EmailAlreadyTakenException.class, () -> userService.registerUser(user.getUsername(), "example@email.com", user.getPassword()));
    }

    @Test
    void shouldThrowExceptionWhenUsernameIsTaken() {
        when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));
        assertThrows(UsernameAlreadyTakenException.class, () -> userService.registerUser("Username1", user.getEmail(), user.getPassword()));
    }

    @Test
    void shouldLoginUserSuccessfully() {
        Authentication authentication = mock(Authentication.class);
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(authenticationManager.authenticate(any(Authentication.class))).thenReturn(authentication);
        when(jwtUtil.generateToken(authentication)).thenReturn("jwtToken");

        String token = userService.loginUser(user.getEmail(), user.getPassword());

        assertEquals("jwtToken", token);
    }

    @Test
    void shouldThrowExceptionWhenPasswordIsWrong() {
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(BadCredentialsException.class);

        assertThrows(BadCredentialsException.class, () -> userService.loginUser(user.getEmail(), "wrongPass2"));
    }
}
