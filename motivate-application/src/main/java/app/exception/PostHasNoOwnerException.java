package app.exception;

public class PostHasNoOwnerException extends RuntimeException {
    public PostHasNoOwnerException(String message) {
        super(message);
    }
}
