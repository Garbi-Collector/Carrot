package gabri.dev.chatapp.repositories;

import gabri.dev.chatapp.entities.User;
import gabri.dev.chatapp.entities.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {

    Optional<VerificationToken> findByToken(String token);

    Optional<VerificationToken> findByUser(User user);

    void deleteByUser(User user);
}