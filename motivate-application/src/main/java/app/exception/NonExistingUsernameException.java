package app.exception;

public class NonExistingUsernameException extends RuntimeException {
    public NonExistingUsernameException(String message) {
        super(message);
    }
}
