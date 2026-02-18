package gabri.dev.chatapp.exceptions;

/**
 * Excepción lanzada cuando el token de verificación es inválido o ya fue usado.
 */
public class InvalidVerificationTokenException extends CarrotException {

    public InvalidVerificationTokenException(String message) {
        super(message);
    }

    public InvalidVerificationTokenException() {
        super("El token de verificación es inválido o ha expirado");
    }
}