package src;

public class CustomExceptions {
    public static class UsernameUnavailableException extends Exception {
        public UsernameUnavailableException(String message) {
            super(message);
        }
    }

    public static class WeakPasswordException extends Exception {
        public WeakPasswordException(String message) {
            super(message);
        }
    }
}
