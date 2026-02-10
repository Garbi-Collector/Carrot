package gabri.dev.chatapp.exceptions;

/**
 * Excepción lanzada cuando se intenta crear una sala de chat que ya existe.
 */
public class ChatRoomAlreadyExistsException extends ChatAppException {

    public ChatRoomAlreadyExistsException(String name) {
        super(String.format("Ya existe una sala de chat con el nombre: '%s'", name));
    }

    public ChatRoomAlreadyExistsException(String message, boolean custom) {
        super(message);
    }
}