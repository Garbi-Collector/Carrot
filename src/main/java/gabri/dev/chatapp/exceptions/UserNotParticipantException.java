package gabri.dev.chatapp.exceptions;

/**
 * Excepción lanzada cuando un usuario intenta acceder a una sala
 * donde no es participante.
 */
public class UserNotParticipantException extends CarrotException {

    public UserNotParticipantException(String username, String chatRoomName) {
        super(String.format("El usuario '%s' no es participante de la sala '%s'",
                username, chatRoomName));
    }

    public UserNotParticipantException(String message) {
        super(message);
    }
}