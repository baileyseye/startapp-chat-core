package startapp.com.core.exception;

import lombok.Getter;

/**
 * Исключение без stack trace для ошибки неверного пароля.
 */
@Getter
public class InvalidPasswordException extends RuntimeException {

    public InvalidPasswordException(String message) {
        super(message);
        setStackTrace(new StackTraceElement[0]);
    }
}
