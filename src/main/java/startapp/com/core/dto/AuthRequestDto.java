package startapp.com.core.dto;

import static lombok.AccessLevel.PRIVATE;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

/**
 * Запрос на аутентификацию пользователя.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = PRIVATE)
public class AuthRequestDto {

    /**
     * Имя пользователя для входа в систему.
     */
    String username;

    /**
     * Пароль пользователя в открытом виде.
     */
    String password;
}
