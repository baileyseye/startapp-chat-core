package startapp.com.core.dto;

import static lombok.AccessLevel.PRIVATE;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

/**
 * DTO результата поиска (чаты и пользователи).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = PRIVATE)
public class SearchResultDto {

    /**
     * Тип результата: "chat" или "user".
     */
    String type;

    /**
     * Идентификатор найденного объекта.
     */
    Long id;

    /**
     * Отображаемое имя чата или пользователя.
     */
    String name;

    /**
     * Текст последнего сообщения (только для чатов).
     */
    String lastMessage;

    /**
     * Время последнего сообщения в ISO-формате (только для чатов).
     */
    String lastMessageTime;
}
