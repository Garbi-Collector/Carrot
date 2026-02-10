package gabri.dev.chatapp.exceptions;

/**
 * Excepción lanzada cuando un usuario no tiene autorización para realizar una acción.
 */
public class UnauthorizedException extends ChatAppException {

    public UnauthorizedException(String message) {
        super(message);
    }

    public UnauthorizedException() {
        super("No tienes autorización para realizar esta acción");
    }
}