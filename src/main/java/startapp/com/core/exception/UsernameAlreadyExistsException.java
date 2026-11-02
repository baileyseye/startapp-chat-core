package startapp.com.core.exception;

import lombok.Getter;

/**
 * Исключение без stack trace для ошибки занятого имени пользователя.
 */
@Getter
public class UsernameAlreadyExistsException extends RuntimeException {

    public UsernameAlreadyExistsException(String message) {
        super(message);
        setStackTrace(new StackTraceElement[0]);
    }
}
