package startapp.com.core.dto;

import static lombok.AccessLevel.PRIVATE;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

/**
 * DTO сообщения.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = PRIVATE)
public class MessageDto {

    /**
     * Уникальный идентификатор сообщения.
     */
    Long id;

    /**
     * Идентификатор чата, в котором было отправлено сообщение.
     */
    Long chatId;

    /**
     * Идентификатор пользователя-отправителя.
     */
    Long senderId;

    /**
     * Имя отправителя для отображения.
     */
    String senderName;

    /**
     * Текст сообщения.
     */
    String content;

    /**
     * Дата и время отправки сообщения.
     */
    LocalDateTime createdAt;

    /**
     * Флаг прочтения сообщения получателем.
     */
    boolean isRead;

    /**
     * Направление сообщения: "inbound" (входящее) или "outbound" (исходящее).
     */
    String direction;
}
