package gabri.dev.chatapp.exceptions;

/**
 * Excepción base para todas las excepciones de la aplicación.
 */
public class ChatAppException extends RuntimeException {

    public ChatAppException(String message) {
        super(message);
    }

    public ChatAppException(String message, Throwable cause) {
        super(message, cause);
    }
}