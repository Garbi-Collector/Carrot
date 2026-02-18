package gabri.dev.chatapp.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Entidad que representa un usuario en el sistema de chat.
 * <p>
 * Almacena información de autenticación, perfil y estado de conexión del usuario.
 * Cada usuario puede participar en múltiples salas de chat y enviar mensajes.
 * </p>
 *
 * <h3>Relaciones:</h3>
 * <ul>
 *   <li>One-to-Many con {@link Message} (mensajes enviados)</li>
 *   <li>Many-to-Many con {@link ChatRoom} (salas de chat)</li>
 * </ul>
 *
 * @author Gabri
 * @version 1.0
 * @since 1.0
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    /**
     * Identificador único del usuario.
     * Generado automáticamente por la base de datos.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Nombre de usuario único en el sistema.
     * <p>
     * Debe ser único y no puede ser nulo.
     * Máximo 50 caracteres.
     * </p>
     */
    @Column(unique = true, nullable = false, length = 50)
    private String username;

    /**
     * Dirección de correo electrónico del usuario.
     * <p>
     * Debe ser única y se utiliza para autenticación y notificaciones.
     * Máximo 100 caracteres.
     * </p>
     */
    @Column(unique = true, nullable = false, length = 100)
    private String email;

    /**
     * Contraseña del usuario encriptada.
     * <p>
     * Debe ser almacenada utilizando un algoritmo de hash seguro (ej: BCrypt).
     * Nunca debe ser expuesta en las respuestas de la API.
     * </p>
     */
    @Column(nullable = false)
    private String password;

    /**
     * Nombre completo del usuario.
     * Campo opcional con máximo 100 caracteres.
     */
    @Column(length = 100)
    private String fullName;

    /**
     * URL de la imagen de perfil del usuario.
     * <p>
     * Puede ser una URL externa o una ruta al sistema de almacenamiento local.
     * Máximo 500 caracteres.
     * </p>
     */
    @Column(length = 500)
    private String avatarUrl;

    /**
     * Estado de conexión actual del usuario.
     * <p>
     * Valores posibles definidos en {@link UserStatus}.
     * Por defecto: {@link UserStatus#OFFLINE}
     * </p>
     *
     * @see UserStatus
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private UserStatus status = UserStatus.OFFLINE;

    /**
     * Indica si la cuenta del usuario está habilitada.
     * <p>
     * Las cuentas deshabilitadas no pueden iniciar sesión.
     * Por defecto: {@code true}
     * </p>
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean enabled = true;

    /**
     * Fecha y hora de creación del usuario.
     * <p>
     * Generado automáticamente al crear el registro.
     * No puede ser modificado después de la creación.
     * </p>
     */
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Fecha y hora de la última actualización del usuario.
     * Actualizado automáticamente cada vez que se modifica el registro.
     */
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    /**
     * Indica si el email del usuario ha sido verificado.
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean emailVerified = false;

    /**
     * Token único para verificación de email.
     */
    @Column(unique = true)
    private String verificationToken;

    /**
     * Fecha y hora de la última vez que el usuario estuvo en línea.
     * <p>
     * Se actualiza cuando el usuario cambia a estado {@link UserStatus#OFFLINE}.
     * Útil para mostrar "Última vez visto hace X tiempo".
     * </p>
     */
    private LocalDateTime lastSeenAt;

    /**
     * Colección de mensajes enviados por este usuario.
     * <p>
     * Relación bidireccional con {@link Message}.
     * Los mensajes son eliminados en cascada si se elimina el usuario.
     * </p>
     *
     * @see Message#sender
     */
    @OneToMany(mappedBy = "sender", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<Message> sentMessages = new HashSet<>();

    /**
     * Colección de salas de chat en las que participa el usuario.
     * <p>
     * Relación bidireccional muchos a muchos con {@link ChatRoom}.
     * Un usuario puede estar en múltiples salas y una sala puede tener múltiples usuarios.
     * </p>
     *
     * @see ChatRoom#participants
     */
    @ManyToMany(mappedBy = "participants")
    @Builder.Default
    private Set<ChatRoom> chatRooms = new HashSet<>();

    /**
     * Enumeración que define los posibles estados de conexión de un usuario.
     * <p>
     * Estos estados permiten a otros usuarios saber la disponibilidad actual.
     * </p>
     */
    public enum UserStatus {
        /**
         * Usuario está conectado y activo en la aplicación.
         */
        ONLINE,

        /**
         * Usuario está desconectado de la aplicación.
         */
        OFFLINE,

        /**
         * Usuario está conectado pero ausente temporalmente.
         */
        AWAY,

        /**
         * Usuario está conectado pero ocupado, no desea ser molestado.
         */
        BUSY
    }
}
