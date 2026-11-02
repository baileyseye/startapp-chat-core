package startapp.com.core.dto;

import static lombok.AccessLevel.PRIVATE;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

/**
 * DTO для пагинированного списка сообщений.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = PRIVATE)
public class MessagePageDto {

    /**
     * Список сообщений на текущей странице.
     */
    List<MessageDto> messages;

    /**
     * Номер текущей страницы (с нуля).
     */
    int pageNumber;

    /**
     * Количество элементов на странице.
     */
    int pageSize;

    /**
     * Общее количество сообщений в чате.
     */
    long totalElements;

    /**
     * Флаг первой страницы.
     */
    boolean isFirst;

    /**
     * Флаг последней страницы.
     */
    boolean isLast;
}
