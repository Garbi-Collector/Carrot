package gabri.dev.chatapp.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Entidad que representa un mensaje en una sala de chat.
 * <p>
 * Cada mensaje está asociado a un usuario emisor y a una sala de chat.
 * Soporta diferentes tipos de contenido mediante {@link MessageType}.
 * </p>
 *
 * <h3>Relaciones:</h3>
 * <ul>
 *   <li>Many-to-One con {@link User} (remitente del mensaje)</li>
 *   <li>Many-to-One con {@link ChatRoom} (sala donde se envió)</li>
 * </ul>
 *
 * @author Gabri
 * @version 1.0
 * @since 1.0
 */
@Entity
@Table(name = "messages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Message {

    /**
     * Identificador único del mensaje.
     * Generado automáticamente por la base de datos.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Contenido textual del mensaje.
     * <p>
     * Puede contener texto, URLs, menciones, etc.
     * Almacenado como TEXT para soportar mensajes largos.
     * </p>
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    /**
     * Tipo de mensaje según {@link MessageType}.
     * <p>
     * Determina cómo debe ser interpretado y mostrado el contenido.
     * Por defecto: {@link MessageType#CHAT}
     * </p>
     *
     * @see MessageType
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private MessageType type = MessageType.CHAT;

    /**
     * Fecha y hora en que fue enviado el mensaje.
     * <p>
     * Generado automáticamente al crear el registro.
     * No puede ser modificado después de la creación.
     * </p>
     */
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime sentAt;

    /**
     * Fecha y hora de la última edición del mensaje.
     * <p>
     * {@code null} si el mensaje nunca ha sido editado.
     * Debe actualizarse manualmente cuando se edita el mensaje.
     * </p>
     */
    @Column
    private LocalDateTime editedAt;

    /**
     * Indica si el mensaje ha sido editado después de su envío.
     * <p>
     * Útil para mostrar un indicador visual de "editado" en la UI.
     * Por defecto: {@code false}
     * </p>
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean isEdited = false;

    /**
     * Usuario que envió el mensaje.
     * <p>
     * Relación Many-to-One con {@link User}.
     * Carga lazy para optimizar consultas.
     * </p>
     *
     * @see User#sentMessages
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    /**
     * Sala de chat donde fue enviado el mensaje.
     * <p>
     * Relación Many-to-One con {@link ChatRoom}.
     * Carga lazy para optimizar consultas.
     * </p>
     *
     * @see ChatRoom#messages
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id", nullable = false)
    private ChatRoom chatRoom;

    /**
     * Enumeración que define los tipos de mensajes soportados.
     * <p>
     * Cada tipo puede requerir un procesamiento diferente en el cliente.
     * </p>
     */
    public enum MessageType {
        /**
         * Mensaje de chat normal enviado por un usuario.
         */
        CHAT,

        /**
         * Mensaje del sistema indicando que un usuario se unió a la sala.
         * El campo {@code content} contiene el nombre del usuario.
         */
        JOIN,

        /**
         * Mensaje del sistema indicando que un usuario abandonó la sala.
         * El campo {@code content} contiene el nombre del usuario.
         */
        LEAVE,

        /**
         * Mensaje generado automáticamente por el sistema.
         * Por ejemplo: "La sala fue creada", "El nombre fue cambiado", etc.
         */
        SYSTEM,

        /**
         * Mensaje que contiene un archivo adjunto.
         * El campo {@code content} contiene la URL o ruta del archivo.
         */
        FILE,

        /**
         * Mensaje que contiene una imagen.
         * El campo {@code content} contiene la URL o ruta de la imagen.
         */
        IMAGE
    }
}
