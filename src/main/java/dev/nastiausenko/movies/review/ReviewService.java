package dev.nastiausenko.movies.review;

import dev.nastiausenko.movies.movie.Movie;
import dev.nastiausenko.movies.review.exception.ForbiddenException;
import dev.nastiausenko.movies.review.exception.ReviewNotFoundException;
import dev.nastiausenko.movies.user.User;
import dev.nastiausenko.movies.user.UserRepository;
import dev.nastiausenko.movies.user.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Update;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final MongoTemplate mongoTemplate;

    public Review createReview(String reviewBody, String imdbId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userRepository.findByUsername(username).orElseThrow(UserNotFoundException::new);
        Review review = reviewRepository.insert(new Review(reviewBody, user.getId()));

        mongoTemplate.update(Movie.class)
                .matching(Criteria.where("imdbId").is(imdbId))
                .apply(new Update().push("reviewIds").value(review))
                .first();

        mongoTemplate.update(User.class)
                .matching(Criteria.where("id").is(user.getId()))
                .apply(new Update().push("reviewIds").value(review))
                .first();

        return review;
    }

    public List<Review> getAllUserReviews() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            ObjectId id = userRepository.findByUsername(username).orElseThrow(UserNotFoundException::new).getId();
            return reviewRepository.findByUserId(id);
        } catch (Exception e) {
            throw new ForbiddenException("Forbidden");
        }
    }

    public Review editReview(ObjectId id, String reviewBody) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userRepository.findByUsername(username).orElseThrow(UserNotFoundException::new);

        Review review = reviewRepository.findById(id).orElseThrow(ReviewNotFoundException::new);

        if (!review.getUserId().equals(user.getId())) {
            throw new ForbiddenException("You can only edit your own reviews");
        }

        review.setBody(reviewBody);
        return reviewRepository.save(review);
    }

    public void deleteReview(ObjectId id, String imdbId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userRepository.findByUsername(username).orElseThrow(UserNotFoundException::new);

        Review review = reviewRepository.findById(id).orElseThrow(ReviewNotFoundException::new);

        if (!review.getUserId().equals(user.getId())) {
            throw new ForbiddenException("You can only delete your own reviews");
        }

        reviewRepository.deleteById(id);

        mongoTemplate.update(Movie.class)
                .matching(Criteria.where("imdbId").is(imdbId))
                .apply(new Update().pull("reviewIds", id))
                .first();

        mongoTemplate.update(User.class)
                .matching(Criteria.where("id").is(user.getId()))
                .apply(new Update().pull("reviewIds", id))
                .first();
    }
}
