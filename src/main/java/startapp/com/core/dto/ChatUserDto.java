package startapp.com.core.dto;

import static lombok.AccessLevel.PRIVATE;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

/**
 * DTO пользователя в контексте чата.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = PRIVATE)
public class ChatUserDto {

    /**
     * Идентификатор пользователя.
     */
    Long id;

    /**
     * Отображаемое имя пользователя.
     */
    String name;

    /**
     * Флаг онлайн-статуса пользователя.
     */
    boolean isOnline;

    /**
     * Детализированный статус: "online", "offline", "typing", "last_seen".
     */
    String status;
}
