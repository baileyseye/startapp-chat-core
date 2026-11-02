package startapp.com.core.controller;

import static org.springframework.http.ResponseEntity.noContent;
import static org.springframework.http.ResponseEntity.ok;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import startapp.com.core.dto.ChatDto;
import startapp.com.core.dto.ChatUserDto;
import startapp.com.core.dto.MessageDto;
import startapp.com.core.dto.MessagePageDto;
import startapp.com.core.dto.SearchResultDto;
import startapp.com.core.mapper.ChatMapper;
import startapp.com.core.model.Chat;
import startapp.com.core.service.ChatService;
import startapp.com.core.service.UserService;

/**
 * Контроллер для работы с чатами и сообщениями
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chats")
public class ChatController {

    private final ChatService chatService;
    private final ChatMapper chatMapper;
    private final UserService userService;

    /**
     * GET {@code http://localhost:8080/api/chats?userId=1}
     * <br>
     * Пример: {@code http://localhost:8080/api/chats?userId=1}
     */
    @GetMapping
    public ResponseEntity<List<ChatDto>> getChatsForUser(@RequestParam Long userId) {
        return ok(chatService.getAllChatsForUser(userId));
    }

    /**
     * GET {@code http://localhost:8080/api/chats/{id}?userId=1}
     * <br>
     * Пример: {@code http://localhost:8080/api/chats/1?userId=1}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ChatDto> getChatById(@PathVariable Long id, @RequestParam Long userId) {
        return ok(chatService.getChatById(id, userId));
    }

    /**
     * GET {@code http://localhost:8080/api/chats/{id}/messages?userId=1}
     * <br>
     * Пример: {@code http://localhost:8080/api/chats/1/messages?userId=1}
     */
    @GetMapping("/{id}/messages")
    public ResponseEntity<List<MessageDto>> getMessages(@PathVariable Long id, @RequestParam Long userId) {
        return ok(chatService.getMessages(id, userId));
    }

    /**
     * Подгружаем по 50 сообщений
     * <br>
     * GET {@code http://localhost:8080/api/chats/{id}/messages/paged?userId=1&page=0&size=50}
     * <br>
     * Пример: {@code http://localhost:8080/api/chats/1/messages/paged?userId=1&page=0&size=50}
     */
    @GetMapping("/{id}/messages/paged")
    public ResponseEntity<MessagePageDto> getMessagesPaged(@PathVariable Long id,
                                                           @RequestParam Long userId,
                                                           @RequestParam(defaultValue = "0") int page,
                                                           @RequestParam(defaultValue = "50") int size) {
        return ok(chatService.getMessagesPaged(id, page, size, userId));
    }

    /**
     * POST {@code http://localhost:8080/api/chats}
     * <br><br>
     * Body:
     * <pre>{@code
     * {
     *   "userId1": 1,
     *   "userId2": 2
     * }
     * }</pre>
     */
    @PostMapping
    public ResponseEntity<ChatDto> createPrivateChat(@RequestBody CreateChatRequest request) {
        return ok(chatService.createPrivateChat(request.userId1(), request.userId2()));
    }

    /**
     * POST {@code http://localhost:8080/api/chats/{id}/messages}
     * <br><br>
     * Body:
     * <pre>{@code
     * {
     *   "senderId": 1,
     *   "content": "Привет, как дела?"
     * }
     * }</pre>
     */
    @PostMapping("/{id}/messages")
    public ResponseEntity<MessageDto> sendMessage(
        @PathVariable Long id,
        @RequestBody SendMessageRequest request) {
        return ok(chatService.sendMessage(id, request.senderId(), request.content()));
    }

    /**
     * GET {@code http://localhost:8080/api/chats/favorites/{userId}}
     * <br>
     * Получает или создает чат "Избранное" для пользователя
     * <br>
     * Пример: {@code http://localhost:8080/api/chats/favorites/1}
     */
    @GetMapping("/favorites/{userId}")
    public ResponseEntity<ChatDto> getOrCreateFavoritesChat(@PathVariable Long userId) {
        Chat favoritesChat = chatService.getOrCreateFavoritesChat(userId);
        return ok(chatMapper.toDto(favoritesChat));
    }

    /**
     * GET {@code http://localhost:8080/api/chats/{id}/participants}
     * <br>
     * Пример: {@code http://localhost:8080/api/chats/1/participants}
     */
    @GetMapping("/{id}/participants")
    public ResponseEntity<List<ChatUserDto>> getChatParticipants(@PathVariable Long id) {
        return ok(chatService.getChatParticipants(id));
    }

    /**
     * GET {@code http://localhost:8080/api/chats/search?userId=1&query=Филяс}
     * <br>
     * Пример: {@code http://localhost:8080/api/chats/search?userId=1&query=Филяс}
     */
    @GetMapping("/search")
    public ResponseEntity<List<SearchResultDto>> searchChatsAndUsers(@RequestParam Long userId,
                                                                     @RequestParam(required = false, defaultValue = "") String query) {
        return ok(chatService.searchChatsAndUsers(userId, query, userService));
    }

    /**
     * POST {@code http://localhost:8080/api/chats/create-with-user}
     * <br><br>
     * Body:
     * <pre>{@code
     * {
     *   "currentUserId": 1,
     *   "otherUserId": 2
     * }
     * }</pre>
     */
    @PostMapping("/create-with-user")
    public ResponseEntity<ChatDto> createChatWithUser(@RequestBody CreateChatWithUserRequest request) {
        Chat chat = chatService.getOrCreatePrivateChat(request.currentUserId(), request.otherUserId());
        return ok(chatMapper.toDto(chat));
    }

    /**
     * DELETE {@code http://localhost:8080/api/chats/{id}?userId=1}
     * <br>
     * Пример: {@code http://localhost:8080/api/chats/1?userId=1}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteChat(@PathVariable Long id, @RequestParam Long userId) {
        chatService.deleteChat(id, userId);
        return noContent().build();
    }

    /**
     * Запрос на создание чата с пользователем
     *
     * @param currentUserId ID текущего пользователя
     * @param otherUserId   ID другого пользователя
     */
    public record CreateChatWithUserRequest(Long currentUserId, Long otherUserId) {

    }

    /**
     * Запрос на создание чата
     *
     * @param userId1 ID первого пользователя
     * @param userId2 ID второго пользователя
     */
    public record CreateChatRequest(Long userId1, Long userId2) {

    }

    /**
     * Запрос на отправку сообщения
     *
     * @param senderId ID отправителя
     * @param content  Текст сообщения
     */
    public record SendMessageRequest(Long senderId, String content) {

    }
}
