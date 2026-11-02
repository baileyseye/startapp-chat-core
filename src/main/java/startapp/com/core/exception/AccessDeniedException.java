package startapp.com.core.exception;

import lombok.Getter;

/**
 * Исключение без stack trace для ошибок доступа.
 * В логах отображается только одна строка сообщения.
 */
@Getter
public class AccessDeniedException extends RuntimeException {

    public AccessDeniedException(String message) {
        super(message);
        setStackTrace(new StackTraceElement[0]);
    }
}
