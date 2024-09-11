package dev.nastiausenko.movies.user.exception;

public class UsernameAlreadyTakenException extends RuntimeException {
    public UsernameAlreadyTakenException(String username) {
        super("Username " + username + " is already taken");
    }
}
