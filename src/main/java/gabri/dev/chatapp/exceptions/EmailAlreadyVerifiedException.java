package gabri.dev.chatapp.exceptions;

/**
 * Excepción lanzada cuando se intenta verificar un email que ya está verificado.
 */
public class EmailAlreadyVerifiedException extends CarrotException {

    public EmailAlreadyVerifiedException(String message) {
        super(message);
    }

    public EmailAlreadyVerifiedException() {
        super("Este email ya ha sido verificado");
    }

    public EmailAlreadyVerifiedException(String email, boolean withEmail) {
        super(String.format("El email '%s' ya ha sido verificado", email));
    }
}