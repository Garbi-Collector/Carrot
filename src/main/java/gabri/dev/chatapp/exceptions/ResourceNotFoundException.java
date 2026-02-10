package gabri.dev.chatapp.exceptions;

/**
 * Excepción lanzada cuando no se encuentra un recurso solicitado.
 */
public class ResourceNotFoundException extends ChatAppException {

    public ResourceNotFoundException(String resource, String field, Object value) {
        super(String.format("%s no encontrado con %s: '%s'", resource, field, value));
    }

    public ResourceNotFoundException(String message) {
        super(message);
    }
}