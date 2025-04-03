package app.exception;

public class NoActiveMembershipException extends RuntimeException {
    public NoActiveMembershipException(String message) {
        super(message);
    }
}
