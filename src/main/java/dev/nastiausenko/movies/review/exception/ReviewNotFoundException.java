package dev.nastiausenko.movies.review.exception;

public class ReviewNotFoundException extends RuntimeException {
    public ReviewNotFoundException() {
        super("Review not found");
    }
}
