package dev.nastiausenko.movies.user;

import dev.nastiausenko.movies.review.Review;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public void registerUser(User request) {
        Optional<User> existingUser = userRepository.findByUsername(request.getUsername());
        if (existingUser.isPresent()) {
            throw new RuntimeException("User with this username already exists");
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword())) // Захист пароля
                .build();

        userRepository.save(user);
    }

    //TODO через авторизацію а не pathVeriable
    public List<Review> getUserReviews(ObjectId id) {
        Optional<User> user = userRepository.findById(id);
        if (user.isPresent()) {
            return user.get().getReviewIds();
        } else {
            throw new RuntimeException("User with this username does not exist");
        }
    }
}
