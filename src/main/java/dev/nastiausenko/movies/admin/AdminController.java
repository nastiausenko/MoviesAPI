package dev.nastiausenko.movies.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@PreAuthorize("hasAuthority('ADMIN')")
@RequestMapping("/api/V1/admin")
public class AdminController {
    private final AdminService adminService;

    @PostMapping("/grant-admin/{username}")
    public ResponseEntity<?> grantAdmin(@PathVariable String username) {
            adminService.grantAdmin(username);
            return ResponseEntity.ok("User " + username + " has been granted admin rights");
    }

    @PostMapping("/revoke-admin/{username}")
    public ResponseEntity<?> revokeAdmin(@PathVariable String username) {
        adminService.revokeAdmin(username);
        return ResponseEntity.ok("User " + username + " has been revoked");
    }
}
