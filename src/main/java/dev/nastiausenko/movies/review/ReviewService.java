package dev.nastiausenko.movies.review;

import dev.nastiausenko.movies.movie.Movie;
import dev.nastiausenko.movies.user.User;
import dev.nastiausenko.movies.user.UserRepository;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Update;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReviewService {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    public Review createReview(String reviewBody, String imdbId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));
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

    public List<Review> getAllUserReviews(ObjectId id) {
        return reviewRepository.findByUserId(id);
    }

    public Review editReview(ObjectId id, String reviewBody) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));

        Review review = reviewRepository.findById(id).orElseThrow(() -> new RuntimeException("Review not found"));

        if (!review.getUserId().equals(user.getId())) {
            throw new RuntimeException("You can only edit your own reviews");
        }

        review.setBody(reviewBody);
        return reviewRepository.save(review);
    }

    public void deleteReview(ObjectId id, String imdbId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));

        Review review = reviewRepository.findById(id).orElseThrow(() -> new RuntimeException("Review not found"));

        if (!review.getUserId().equals(user.getId())) {
            throw new RuntimeException("You can only delete your own reviews");
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
