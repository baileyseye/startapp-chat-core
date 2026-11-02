package startapp.com.core.dto;

import static lombok.AccessLevel.PRIVATE;

import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

/**
 * DTO для чата.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = PRIVATE)
public class ChatDto {

    /**
     * Идентификатор чата.
     */
    Long id;

    /**
     * Название чата.
     */
    String title;

    /**
     * Тип чата: "private", "group", "channel", "system", "favorites".
     */
    String type;

    /**
     * Идентификатор владельца чата (для чатов типа "favorites").
     */
    Long ownerId;

    /**
     * Список участников чата с их статусами.
     */
    List<ChatUserDto> participants;

    /**
     * Текст последнего сообщения в чате.
     */
    String lastMessage;

    /**
     * Время отправки последнего сообщения.
     */
    LocalDateTime lastMessageTime;

    /**
     * Количество непрочитанных сообщений для текущего пользователя.
     */
    Integer unreadCount;
}
