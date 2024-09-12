package dev.nastiausenko.movies.admin;

import dev.nastiausenko.movies.user.User;
import dev.nastiausenko.movies.user.UserRepository;
import dev.nastiausenko.movies.user.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;

    public void grantAdmin(String username) {
        User user = userRepository.findByUsername(username).orElseThrow(UserNotFoundException::new);

        if (!user.getRoles().contains("ADMIN")) {
            user.getRoles().add("ADMIN");
            userRepository.save(user);
        } else {
            //TODO exception
            throw new RuntimeException("User already has admin rights");
        }
    }

    public void revokeAdmin(String username) {
        User user = userRepository.findByUsername(username).orElseThrow(UserNotFoundException::new);

        if (user.getRoles().contains("ADMIN")) {
            user.getRoles().remove("ADMIN");
            userRepository.save(user);
        } else {
            //TODO exception
            throw new RuntimeException("User doesn't have admin rights");
        }
    }
}
