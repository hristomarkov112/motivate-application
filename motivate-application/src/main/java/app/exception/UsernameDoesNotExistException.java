package app.exception;

public class UsernameDoesNotExistException extends RuntimeException {
    public UsernameDoesNotExistException(String message) {
        super(message);
    }
}
