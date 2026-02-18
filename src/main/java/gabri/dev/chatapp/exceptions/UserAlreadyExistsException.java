package gabri.dev.chatapp.exceptions;

/**
 * Excepción lanzada cuando se intenta crear un usuario que ya existe.
 */
public class UserAlreadyExistsException extends CarrotException {

    public UserAlreadyExistsException(String field, String value) {
        super(String.format("Ya existe un usuario con %s: '%s'", field, value));
    }

    public UserAlreadyExistsException(String message) {
        super(message);
    }
}