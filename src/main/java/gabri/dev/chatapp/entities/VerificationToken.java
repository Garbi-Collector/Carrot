package gabri.dev.chatapp.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entidad para tokens de verificación de email.
 * Los tokens no expiran ya que solo verifican que el email es válido.
 */
@Entity
@Table(name = "verification_tokens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VerificationToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Token único para verificación.
     */
    @Column(nullable = false, unique = true)
    private String token;

    /**
     * Usuario asociado al token.
     */
    @OneToOne(targetEntity = User.class, fetch = FetchType.EAGER)
    @JoinColumn(nullable = false, name = "user_id")
    private User user;

    /**
     * Fecha de creación del token.
     */
    @Column(nullable = false)
    private LocalDateTime createdAt;

    /**
     * Indica si el token ya fue usado.
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean used = false;
}