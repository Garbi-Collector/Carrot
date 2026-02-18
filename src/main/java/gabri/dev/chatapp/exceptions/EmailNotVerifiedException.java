package gabri.dev.chatapp.exceptions;

/**
 * Excepción lanzada cuando un usuario intenta hacer login sin verificar su email.
 */
public class EmailNotVerifiedException extends CarrotException {

    public EmailNotVerifiedException(String message) {
        super(message);
    }

    public EmailNotVerifiedException() {
        super("Debes verificar tu email antes de iniciar sesión");
    }
}