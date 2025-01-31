package dev.nastiausenko.movies.review;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = "*")
@Validated
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @SecurityRequirement(name = "JWT")
    @PostMapping
    public ResponseEntity<Review> addReview(@RequestBody @Valid Map<String, String> payload) {
        return new ResponseEntity<>(reviewService.createReview(payload.get("reviewBody"), payload.get("imdbId")), HttpStatus.OK);
    }

    @SecurityRequirement(name = "JWT")
    @GetMapping("/user-reviews")
    public ResponseEntity<List<Review>> getAllUserReviews() {
        return new ResponseEntity<>(reviewService.getAllUserReviews(), HttpStatus.OK);
    }

    @SecurityRequirement(name = "JWT")
    @PatchMapping("/{id}")
    public ResponseEntity<Review> updateReview(@PathVariable("id") ObjectId id, @RequestBody @Valid Map<String, String> payload) {
        Review updatedReview = reviewService.editReview(id, payload.get("reviewBody"));
        return new ResponseEntity<>(updatedReview, HttpStatus.OK);
    }

    @SecurityRequirement(name = "JWT")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteReview(@PathVariable("id") ObjectId id, @RequestParam String imdbId) {
        reviewService.deleteReview(id, imdbId);
        return ResponseEntity.noContent().build();
    }
}
