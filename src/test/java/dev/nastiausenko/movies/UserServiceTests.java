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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
class UserServiceTests {

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
    void setUp() {
        user = User.builder()
                .name("Username1")
                .email("example@email.com")
                .password("Password07")
                .roles(Set.of("USER"))
                .isBlocked(false)
                .build();
    }

    @Test
    void shouldRegisterUserSuccessfully() {
        when(userRepository.findByName(user.getName())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(user.getPassword())).thenReturn("encodedPassword");
        when(jwtUtil.generateToken(any())).thenReturn("JWT");

        String token = userService.registerUser(user.getName(), user.getEmail(), user.getPassword());

        assertNotNull(token);
        verify(userRepository).save(any(User.class));
        verify(userRepository).findByEmail(user.getEmail());
    }

    @Test
    void shouldThrowExceptionWhenEmailIsAlreadyTaken() {
       when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
       assertThrows(EmailAlreadyTakenException.class, () -> userService.registerUser(user.getName(), "example@email.com", user.getPassword()));
    }

    @Test
    void shouldThrowExceptionWhenUsernameIsTaken() {
        when(userRepository.findByName(user.getName())).thenReturn(Optional.of(user));
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.empty());

        assertThrows(UsernameAlreadyTakenException.class,
                () -> userService.registerUser(user.getName(), user.getEmail(), user.getPassword()));
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


    @Test
    void shouldThrowExceptionWhenUsernameIsAlreadyTaken() {
        String takenUsername = "takenUsername";

        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn(user.getName());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        when(userRepository.findByName(user.getName())).thenReturn(Optional.of(user));
        when(userRepository.findByName(takenUsername)).thenReturn(Optional.of(mock(User.class)));

        assertThrows(UsernameAlreadyTakenException.class, () -> userService.editUsername(takenUsername));
    }

    @Test
    void shouldEditUsernameSuccessfully() {
        String newUsername = "NewUsername";

        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn(user.getName());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        when(userRepository.findByName(user.getName())).thenReturn(Optional.of(user));
        when(userRepository.findByName(newUsername)).thenReturn(Optional.empty());

        userService.editUsername(newUsername);

        assertEquals(newUsername, user.getName());
        verify(userRepository).save(user);
    }
}
