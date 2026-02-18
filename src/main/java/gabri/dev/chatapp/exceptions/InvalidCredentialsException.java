package gabri.dev.chatapp.exceptions;

/**
 * Excepción lanzada cuando las credenciales de login son inválidas.
 */
public class InvalidCredentialsException extends CarrotException {

    public InvalidCredentialsException() {
        super("Credenciales inválidas. Verifica tu usuario/email y contraseña");
    }

    public InvalidCredentialsException(String message) {
        super(message);
    }
}