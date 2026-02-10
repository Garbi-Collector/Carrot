package gabri.dev.chatapp.exceptions;

/**
 * Excepción lanzada cuando no se encuentra un mensaje.
 */
public class MessageNotFoundException extends ResourceNotFoundException {

    public MessageNotFoundException(Long messageId) {
        super("Mensaje", "id", messageId);
    }

    public MessageNotFoundException(String message) {
        super(message);
    }
}