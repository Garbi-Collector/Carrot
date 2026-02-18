package gabri.dev.chatapp.exceptions;

/**
 * Excepción base para todas las excepciones de la aplicación.
 */
public class CarrotException extends RuntimeException {

    public CarrotException(String message) {
        super(message);
    }

    public CarrotException(String message, Throwable cause) {
        super(message, cause);
    }
}