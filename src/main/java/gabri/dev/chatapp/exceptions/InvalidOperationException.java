package gabri.dev.chatapp.exceptions;

/**
 * Excepción lanzada cuando se intenta realizar una operación inválida.
 */
public class InvalidOperationException extends ChatAppException {

    public InvalidOperationException(String message) {
        super(message);
    }
}