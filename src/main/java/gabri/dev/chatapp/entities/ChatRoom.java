package gabri.dev.chatapp.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Entidad que representa una sala de chat.
 * Puede ser privada (1 a 1) o grupal (múltiples usuarios).
 */
@Entity
@Table(name = "chat_rooms")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ChatRoomType type = ChatRoomType.PRIVATE;

    @Column(length = 500)
    private String description;

    @Column(length = 500)
    private String imageUrl;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    // Participantes de la sala
    @ManyToMany
    @JoinTable(
            name = "chat_room_participants",
            joinColumns = @JoinColumn(name = "chat_room_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    @Builder.Default
    private Set<User> participants = new HashSet<>();

    // Mensajes de la sala
    @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<Message> messages = new HashSet<>();

    // Usuario creador de la sala (para salas grupales)
    @ManyToOne
    @JoinColumn(name = "created_by_user_id")
    private User createdBy;

    /**
     * Enum para el tipo de sala de chat.
     */
    public enum ChatRoomType {
        PRIVATE,    // Chat 1 a 1
        GROUP       // Chat grupal
    }
}