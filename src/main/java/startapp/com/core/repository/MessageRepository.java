package startapp.com.core.repository;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import startapp.com.core.model.Message;

public interface MessageRepository extends JpaRepository<Message, Long> {

    List<Message> findByChatIdOrderByCreatedAtAsc(Long chatId);

    @Query("SELECT m FROM Message m WHERE m.chat.id = :chatId ORDER BY m.createdAt ASC")
    Page<Message> findByChatIdPaged(@Param("chatId") Long chatId, Pageable pageable);
}
