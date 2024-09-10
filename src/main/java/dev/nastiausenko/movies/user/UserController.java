package dev.nastiausenko.movies.user;

import dev.nastiausenko.movies.user.dto.UserResponse;
import dev.nastiausenko.movies.jwt.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/V1")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final MongoUserDetailsService userDetailsService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User request) {
        try {
            userService.registerUser(request);

            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);

            String jwtToken = jwtUtil.generateToken(authentication);

            return new ResponseEntity<>(new UserResponse(jwtToken), HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);

            String jwtToken = jwtUtil.generateToken(authentication);
            return new ResponseEntity<>(new UserResponse(jwtToken), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Invalid credentials", HttpStatus.UNAUTHORIZED);
        }
    }

    @PutMapping("/change-username")
    public ResponseEntity<?> update(@RequestBody ChangeUsernameRequest request) {
        try {
            userService.editUsername(request.getNewUsername());
            String token = getNewToken(request.getNewUsername());
            return new ResponseEntity<>(new UserResponse(token), HttpStatus.OK);
        } catch(RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.UNAUTHORIZED);
        }
    }

    @PutMapping("/change-password")
    public ResponseEntity<?> updatePassword(@RequestBody ChangePasswordRequest request) {
        try {
            userService.editPassword(request.getNewPassword());
            return new ResponseEntity<>(new UserResponse(null), HttpStatus.OK);
        } catch(RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.UNAUTHORIZED);
        }
    }

    private String getNewToken(String username) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(userDetails.getUsername(),
                userDetails.getPassword(), userDetails.getAuthorities());

        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        return jwtUtil.generateToken(authenticationToken);
    }
}
