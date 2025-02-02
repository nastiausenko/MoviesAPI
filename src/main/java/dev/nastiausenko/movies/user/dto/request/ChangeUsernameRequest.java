package dev.nastiausenko.movies.user.dto.request;

import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChangeUsernameRequest {

    @Pattern(regexp = "^[A-Za-z][A-Za-z0-9_]{7,29}$")@Pattern(regexp = "^[A-Za-z][A-Za-z0-9_]{8,30}$",
            message = "Username must contain minimum eight characters")
    private String newUsername;
}
