package gabri.dev.chatapp.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Entidad que representa un mensaje en una sala de chat.
 */
@Entity
@Table(name = "messages")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private MessageType type = MessageType.CHAT;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime sentAt;

    @Column
    private LocalDateTime editedAt;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isEdited = false;

    // Usuario que envía el mensaje
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    // Sala de chat donde se envía el mensaje
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id", nullable = false)
    private ChatRoom chatRoom;

    /**
     * Enum para los tipos de mensajes.
     */
    public enum MessageType {
        CHAT,           // Mensaje normal de chat
        JOIN,           // Usuario se une a la sala
        LEAVE,          // Usuario abandona la sala
        SYSTEM,         // Mensaje del sistema
        FILE,           // Archivo adjunto
        IMAGE           // Imagen
    }
}