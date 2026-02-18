package gabri.dev.chatapp.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Entidad que representa una sala de chat.
 * <p>
 * Una sala puede ser privada (conversación 1 a 1) o grupal (múltiples usuarios).
 * Las salas grupales tienen un creador y pueden tener nombre, descripción e imagen.
 * </p>
 *
 * <h3>Tipos de sala:</h3>
 * <ul>
 *   <li>{@link ChatRoomType#PRIVATE}: Chat entre dos usuarios exactamente</li>
 *   <li>{@link ChatRoomType#GROUP}: Chat con múltiples participantes</li>
 * </ul>
 *
 * <h3>Relaciones:</h3>
 * <ul>
 *   <li>Many-to-Many con {@link User} (participantes)</li>
 *   <li>One-to-Many con {@link Message} (mensajes de la sala)</li>
 *   <li>Many-to-One con {@link User} (creador de la sala)</li>
 * </ul>
 *
 * @author Gabri
 * @version 1.0
 * @since 1.0
 */
@Entity
@Table(name = "chat_rooms")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatRoom {

    /**
     * Identificador único de la sala de chat.
     * Generado automáticamente por la base de datos.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Nombre de la sala de chat.
     * <p>
     * Debe ser único en el sistema. Máximo 100 caracteres.
     * Para salas privadas, puede ser generado automáticamente.
     * Para salas grupales, es definido por el creador.
     * </p>
     */
    @Column(nullable = false, unique = true, length = 100)
    private String name;

    /**
     * Tipo de sala según {@link ChatRoomType}.
     * <p>
     * Determina el comportamiento y las reglas de la sala.
     * Por defecto: {@link ChatRoomType#PRIVATE}
     * </p>
     *
     * @see ChatRoomType
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ChatRoomType type = ChatRoomType.PRIVATE;

    /**
     * Descripción de la sala de chat.
     * <p>
     * Campo opcional, principalmente para salas grupales.
     * Máximo 500 caracteres.
     * </p>
     */
    @Column(length = 500)
    private String description;

    /**
     * URL de la imagen representativa de la sala.
     * <p>
     * Campo opcional, principalmente para salas grupales.
     * Puede ser una URL externa o una ruta al almacenamiento local.
     * Máximo 500 caracteres.
     * </p>
     */
    @Column(length = 500)
    private String imageUrl;

    /**
     * Fecha y hora de creación de la sala.
     * <p>
     * Generado automáticamente al crear el registro.
     * No puede ser modificado después de la creación.
     * </p>
     */
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Fecha y hora de la última actualización de la sala.
     * <p>
     * Actualizado automáticamente cuando se modifican datos de la sala
     * (nombre, descripción, imagen, participantes, etc.).
     * </p>
     */
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    /**
     * Conjunto de usuarios que participan en esta sala.
     * <p>
     * Relación Many-to-Many con {@link User}.
     * La tabla intermedia {@code chat_room_participants} gestiona la relación.
     * </p>
     *
     * <p>
     * <strong>Reglas de negocio:</strong>
     * </p>
     * <ul>
     *   <li>Salas {@link ChatRoomType#PRIVATE}: Exactamente 2 participantes</li>
     *   <li>Salas {@link ChatRoomType#GROUP}: 3 o más participantes</li>
     * </ul>
     *
     * @see User#chatRooms
     */
    @ManyToMany
    @JoinTable(
            name = "chat_room_participants",
            joinColumns = @JoinColumn(name = "chat_room_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    @Builder.Default
    private Set<User> participants = new HashSet<>();

    /**
     * Colección de mensajes enviados en esta sala.
     * <p>
     * Relación bidireccional One-to-Many con {@link Message}.
     * Los mensajes son eliminados en cascada si se elimina la sala.
     * </p>
     *
     * @see Message#chatRoom
     */
    @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<Message> messages = new HashSet<>();

    /**
     * Usuario que creó la sala de chat.
     * <p>
     * Solo aplica para salas de tipo {@link ChatRoomType#GROUP}.
     * El creador típicamente tiene permisos especiales (editar sala, eliminar, etc.).
     * Puede ser {@code null} para salas privadas generadas automáticamente.
     * </p>
     */
    @ManyToOne
    @JoinColumn(name = "created_by_user_id")
    private User createdBy;

    /**
     * Enumeración que define los tipos de salas de chat disponibles.
     * <p>
     * El tipo determina las reglas y comportamiento de la sala.
     * </p>
     */
    public enum ChatRoomType {
        /**
         * Sala de chat privada entre dos usuarios.
         * <p>
         * Características:
         * </p>
         * <ul>
         *   <li>Exactamente 2 participantes</li>
         *   <li>No tiene nombre personalizable (se genera automáticamente)</li>
         *   <li>No puede tener descripción ni imagen personalizadas</li>
         * </ul>
         */
        PRIVATE,

        /**
         * Sala de chat grupal con múltiples participantes.
         * <p>
         * Características:
         * </p>
         * <ul>
         *   <li>3 o más participantes</li>
         *   <li>Tiene nombre personalizable</li>
         *   <li>Puede tener descripción e imagen</li>
         *   <li>Tiene un creador con permisos especiales</li>
         * </ul>
         */
        GROUP
    }
}
