package dev.nastiausenko.movies.user;

import dev.nastiausenko.movies.category.Category;
import dev.nastiausenko.movies.jwt.JwtUtil;
import dev.nastiausenko.movies.user.dto.request.ChangePasswordRequest;
import dev.nastiausenko.movies.user.dto.request.ChangeUsernameRequest;
import dev.nastiausenko.movies.user.dto.response.UserResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/V1/user")
@CrossOrigin(origins = "*")
@Tag(name = "User", description = "The User API")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final MongoUserDetailsService userDetailsService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User request) {
            String jwtToken = userService.registerUser(request);
            return new ResponseEntity<>(new UserResponse(jwtToken), HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User request) {
       String jwtToken = userService.loginUser(request);
       return new ResponseEntity<>(new UserResponse(jwtToken), HttpStatus.OK);
    }

    @SecurityRequirement(name = "JWT")
    @PatchMapping("/change-username")
    public ResponseEntity<?> update(@RequestBody ChangeUsernameRequest request) {
            userService.editUsername(request.getNewUsername());
            String token = getNewToken(request.getNewUsername());
            return new ResponseEntity<>(new UserResponse(token), HttpStatus.OK);
    }

    @SecurityRequirement(name = "JWT")
    @PatchMapping("/change-password")
    public ResponseEntity<?> updatePassword(@RequestBody ChangePasswordRequest request) {
            userService.editPassword(request.getNewPassword());
            return new ResponseEntity<>(new UserResponse(null), HttpStatus.OK);
    }

    @SecurityRequirement(name = "JWT")
    @GetMapping("/categories")
    public ResponseEntity<?> getUserCategories() {
        List<Category> categories = userService.getCategories();
        return new ResponseEntity<>(categories, HttpStatus.OK);
    }

    private String getNewToken(String username) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(userDetails.getUsername(),
                userDetails.getPassword(), userDetails.getAuthorities());

        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        return jwtUtil.generateToken(authenticationToken);
    }
}
