package startapp.com.core.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import startapp.com.core.model.Chat;

public interface ChatRepository extends JpaRepository<Chat, Long> {

    List<Chat> findByParticipantsId(Long userId);

    List<Chat> findAllByType(String type);

    List<Chat> findByOwnerId(Long userId);

    Optional<Chat> findByTypeAndOwnerIdIsNull(String type);

    @Query("SELECT DISTINCT c FROM Chat c JOIN c.participants c_participants WHERE c_participants.name LIKE %:name%")
    List<Chat> findByParticipantNameContainingIgnoreCase(@Param("name") String name);

    void deleteById(Long id);
}
