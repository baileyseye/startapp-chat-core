package startapp.com.core.dto;

import static lombok.AccessLevel.PRIVATE;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

/**
 * DTO пользователя.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = PRIVATE)
public class UserDto {

    /**
     * Уникальный идентифификатор пользователя.
     */
    Long id;

    /**
     * Полное имя пользователя для отображения.
     */
    String name;

    /**
     * Возраст пользователя.
     */
    Integer age;
}
