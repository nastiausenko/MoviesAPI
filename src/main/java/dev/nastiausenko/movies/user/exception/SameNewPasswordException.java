package dev.nastiausenko.movies.user.exception;

//BAD_CREDENTIALS
public class SameNewPasswordException extends RuntimeException {
    public SameNewPasswordException() {
        super("New password matches old password");
    }
}
