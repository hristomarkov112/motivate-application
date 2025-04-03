package app.exception;

public class PostMustNotBeEmpty extends RuntimeException {
    public PostMustNotBeEmpty(String message) {
        super(message);
    }
}
