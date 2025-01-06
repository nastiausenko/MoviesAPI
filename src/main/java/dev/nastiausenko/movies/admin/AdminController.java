package dev.nastiausenko.movies.admin;

import dev.nastiausenko.movies.movie.Movie;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@PreAuthorize("hasAuthority('ADMIN')")
@RequestMapping("/api/v1/admin")
public class AdminController {
    private final AdminService adminService;

    @SecurityRequirement(name = "JWT")
    @PostMapping("/grant-admin/{username}")
    public ResponseEntity<?> grantAdmin(@PathVariable String username) {
            adminService.grantAdmin(username);
            return ResponseEntity.ok("User " + username + " has been granted admin rights");
    }

    @SecurityRequirement(name = "JWT")
    @PostMapping("/revoke-admin/{username}")
    public ResponseEntity<?> revokeAdmin(@PathVariable String username) {
        adminService.revokeAdmin(username);
        return ResponseEntity.ok("User " + username + " has been revoked");
    }

    @SecurityRequirement(name = "JWT")
    @PostMapping("/add-movie")
    public ResponseEntity<?> addMovie(@RequestBody Movie movie) {
        return new ResponseEntity<>(adminService.addMovie(movie), HttpStatus.OK);
    }

    @SecurityRequirement(name = "JWT")
    @PutMapping("/edit-movie/{imdbId}")
    public ResponseEntity<?> editMovie(@PathVariable String imdbId, @RequestBody Movie movie) {
        return new ResponseEntity<>(adminService.editMovie(imdbId, movie), HttpStatus.OK);
    }

    @SecurityRequirement(name = "JWT")
    @PatchMapping("/delete-movie/{imdbId}")
    public ResponseEntity<?> deleteMovie(@PathVariable String imdbId) {
       adminService.deleteMovie(imdbId);
       return ResponseEntity.ok("Movie " + imdbId + " has been deleted");
    }

    @SecurityRequirement(name = "JWT")
    @GetMapping("/all-users")
    public ResponseEntity<?> getAllUsers() {
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    @SecurityRequirement(name = "JWT")
    @PostMapping("/block-user/{username}")
    public ResponseEntity<?> blockUser(@PathVariable String username) {
        adminService.blockUser(username);
        return ResponseEntity.ok("User " + username + " has been blocked");
    }

    @SecurityRequirement(name = "JWT")
    @PostMapping("/unlock-user/{username}")
    public ResponseEntity<?> revokeUser(@PathVariable String username) {
        adminService.unlockUser(username);
        return ResponseEntity.ok("User " + username + " has been unlocked");
    }
}
