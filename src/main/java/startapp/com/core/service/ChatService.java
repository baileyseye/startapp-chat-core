package startapp.com.core.service;

import static org.springframework.security.oauth2.core.OAuth2ErrorCodes.ACCESS_DENIED;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import startapp.com.core.dto.ChatDto;
import startapp.com.core.dto.ChatUserDto;
import startapp.com.core.dto.MessageDto;
import startapp.com.core.dto.MessagePageDto;
import startapp.com.core.dto.SearchResultDto;
import startapp.com.core.dto.UserDto;
import startapp.com.core.exception.AccessDeniedException;
import startapp.com.core.exception.ChatNotFoundException;
import startapp.com.core.exception.UserNotFoundException;
import startapp.com.core.mapper.ChatMapper;
import startapp.com.core.model.Chat;
import startapp.com.core.model.Message;
import startapp.com.core.model.User;
import startapp.com.core.repository.ChatRepository;
import startapp.com.core.repository.MessageRepository;
import startapp.com.core.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class ChatService {

    private static final String CHAT_NOT_FOUND = "Chat not found: ";
    private static final String FAVORITES_CHAT_TYPE = "favorites";
    private static final String USER_NOT_FOUND = "User not found: ";
    private static final String PRIVATE = "private";
    private static final String SYSTEM = "system";

    private final ChatRepository chatRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final ChatMapper chatMapper;

    @Transactional(readOnly = true)
    public List<ChatDto> getAllChatsForUser(Long userId) {
        List<Chat> userChats = chatRepository.findByParticipantsId(userId);
        List<Chat> systemChats = chatRepository.findAllByType(SYSTEM);

        boolean hasFavorites = userChats.stream()
            .anyMatch(chat -> FAVORITES_CHAT_TYPE.equals(chat.getType()));

        if (!hasFavorites) {
            chatRepository.findByOwnerId(userId).stream()
                .filter(chat -> FAVORITES_CHAT_TYPE.equals(chat.getType()))
                .findFirst()
                .ifPresent(userChats::add);
        }

        userChats.addAll(systemChats);

        return chatMapper.toDtoListWithUser(userChats, userId);
    }

    @Transactional
    public Chat getOrCreateFavoritesChat(Long userId) {
        List<Chat> userFavorites = chatRepository.findByOwnerId(userId);

        Chat existingFavorites = userFavorites.stream()
            .filter(chat -> FAVORITES_CHAT_TYPE.equals(chat.getType()))
            .findFirst()
            .orElse(null);

        if (existingFavorites != null) {
            return existingFavorites;
        }

        Chat template = chatRepository.findByTypeAndOwnerIdIsNull(FAVORITES_CHAT_TYPE)
            .orElseThrow(() -> new ChatNotFoundException("Favorites chat template not found"));

        User owner = userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND + userId));

        Chat favoritesChat = Chat.builder()
            .title(template.getTitle())
            .type(template.getType())
            .owner(owner)
            .isDeletable(false)
            .participants(new ArrayList<>(List.of(owner)))
            .messages(new ArrayList<>())
            .build();

        return chatRepository.save(favoritesChat);
    }

    @Transactional(readOnly = true)
    public ChatDto getChatById(Long chatId, Long userId) {
        Chat chat = chatRepository.findById(chatId)
            .orElseThrow(() -> new ChatNotFoundException(CHAT_NOT_FOUND + chatId));

        return chatMapper.toDtoWithUser(chat, userId);
    }

    @Transactional(readOnly = true)
    public List<MessageDto> getMessages(Long chatId, Long userId) {
        getChatAndVerifyParticipant(chatId, userId);
        return chatMapper.toMessageDtoList(
            messageRepository.findByChatIdOrderByCreatedAtAsc(chatId)
        );
    }

    @Transactional(readOnly = true)
    public MessagePageDto getMessagesPaged(Long chatId, int page, int size, Long userId) {
        getChatAndVerifyParticipant(chatId, userId);

        Page<Message> messagePage = messageRepository.findByChatIdPaged(chatId,
            PageRequest.of(page, size,
                Sort.by(Sort.Direction.ASC, "createdAt"))
        );

        List<MessageDto> messageDtos = chatMapper.toMessageDtoList(messagePage.getContent(), userId);

        return MessagePageDto.builder()
            .messages(messageDtos)
            .pageNumber(messagePage.getNumber())
            .pageSize(messagePage.getSize())
            .totalElements(messagePage.getTotalElements())
            .isFirst(messagePage.isFirst())
            .isLast(messagePage.isLast())
            .build();
    }

    /**
     * Проверяет, что пользователь является участником чата, или владельцем (для чатов типа "favorites").
     * Для системных чатов проверка пропускается.
     *
     * @throws ChatNotFoundException если чат не найден
     * @throws AccessDeniedException если пользователь не имеет доступа к чату
     */
    private void getChatAndVerifyParticipant(Long chatId, Long userId) {
        Chat chat = chatRepository.findById(chatId)
            .orElseThrow(() -> new ChatNotFoundException(CHAT_NOT_FOUND + chatId));

        // Системные чаты доступны всем (общая история для всех)
        if (SYSTEM.equals(chat.getType())) {
            return;
        }

        // "Избранное" и другие чаты с owner проверяются по владельцу
        if (chat.getOwner() != null && chat.getOwner().getId().equals(userId)) {
            return;
        }

        // Приватные чаты проверяются по участникам
        boolean isParticipant = chat.getParticipants().stream()
            .anyMatch(user -> user.getId().equals(userId));

        if (!isParticipant) {
            throw new AccessDeniedException("Доступ к чату запрещён: пользователь не является участником");
        }
    }

    @Transactional
    public MessageDto sendMessage(Long chatId, Long senderId, String content) {
        Chat chat = chatRepository.findById(chatId)
            .orElseThrow(() -> new ChatNotFoundException(CHAT_NOT_FOUND + chatId));

        User sender = userRepository.findById(senderId)
            .orElseThrow(() -> new UserNotFoundException("User not found with id " + senderId));

        Message message = Message.builder()
            .chat(chat)
            .sender(sender)
            .content(content)
            .createdAt(LocalDateTime.now())
            .isRead(false)
            .build();

        Message saved = messageRepository.save(message);

        chat.setLastMessage(content);
        chat.setLastMessageTime(LocalDateTime.now());
        chatRepository.save(chat);

        return chatMapper.toMessageDto(saved);
    }

    @Transactional
    public ChatDto createPrivateChat(Long userId1, Long userId2) {
        User user1 = userRepository.findById(userId1)
            .orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND + userId1));

        User user2 = userRepository.findById(userId2)
            .orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND + userId2));

        Chat chat = Chat.builder()
            .title(user2.getName())
            .type(PRIVATE)
            .participants(new ArrayList<>(List.of(user1, user2)))
            .messages(new ArrayList<>())
            .build();

        Chat saved = chatRepository.save(chat);
        return chatMapper.toDto(saved);
    }

    @Transactional(readOnly = true)
    public List<ChatUserDto> getChatParticipants(Long chatId) {
        Chat chat = chatRepository.findById(chatId)
            .orElseThrow(() -> new ChatNotFoundException(CHAT_NOT_FOUND + chatId));

        return chatMapper.toChatUserDtoList(chat.getParticipants());
    }

    private List<SearchResultDto> buildChatSearchResults(Long userId, String query) {
        List<SearchResultDto> results = new ArrayList<>();

        if (query == null || query.isBlank()) {
            return results;
        }

        String trimmedQuery = query.trim();

        List<Chat> chats = chatRepository.findByParticipantNameContainingIgnoreCase(trimmedQuery);
        List<Chat> userChats = chats.stream()
            .filter(chat -> chat.getParticipants().stream()
                .anyMatch(p -> p.getId().equals(userId)))
            .sorted((c1, c2) -> {
                LocalDateTime t1 = c1.getLastMessageTime();
                LocalDateTime t2 = c2.getLastMessageTime();

                if (t1 == null && t2 == null) {
                    return 0;
                }

                if (t1 == null) {
                    return 1;
                }

                if (t2 == null) {
                    return -1;
                }

                return t2.compareTo(t1);
            })
            .toList();

        for (Chat chat : userChats) {
            ChatDto chatDto = chatMapper.toDtoWithUser(chat, userId);
            results.add(SearchResultDto.builder()
                .type("chat")
                .id(chatDto.getId())
                .name(chatDto.getTitle())
                .lastMessage(chatDto.getLastMessage())
                .lastMessageTime(chatDto.getLastMessageTime() != null ? chatDto.getLastMessageTime().toString() : null)
                .build());
        }

        return results;
    }

    @Transactional
    public Chat getOrCreatePrivateChat(Long currentUserId, Long otherUserId) {
        User currentUser = userRepository.findById(currentUserId)
            .orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND + currentUserId));
        User otherUser = userRepository.findById(otherUserId)
            .orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND + otherUserId));

        List<Chat> existingChats = chatRepository.findByParticipantsId(currentUserId);

        for (Chat chat : existingChats) {
            boolean hasOtherUser = chat.getParticipants().stream()
                .anyMatch(p -> p.getId().equals(otherUserId));

            if (hasOtherUser && PRIVATE.equals(chat.getType())) {
                return chat;
            }
        }

        Chat chat = Chat.builder()
            .title(otherUser.getName())
            .type(PRIVATE)
            .participants(new ArrayList<>(List.of(currentUser, otherUser)))
            .messages(new ArrayList<>())
            .build();

        return chatRepository.save(chat);
    }

    @Transactional(readOnly = true)
    public List<SearchResultDto> searchChatsAndUsers(Long userId, String query, UserService userService) {
        List<SearchResultDto> results = buildChatSearchResults(userId, query);

        List<UserDto> userDtos = userService.searchUsers(query != null
            ? query.trim()
            : "");

        for (UserDto user : userDtos) {
            if (!user.getId().equals(userId)) {
                results.add(SearchResultDto.builder()
                    .type("user")
                    .id(user.getId())
                    .name(user.getName())
                    .build());
            }
        }

        return results;
    }

    /**
     * Удаляет чат и все связанные сообщения.
     * Проверка на возможность удаления через флаг isDeletable.
     *
     * @param chatId ID чата для удаления
     * @param userId ID пользователя, который пытается удалить чат
     * @throws ChatNotFoundException если чат не найден
     * @throws AccessDeniedException если чат нельзя удалить или пользователь не имеет доступа
     */
    @Transactional
    public void deleteChat(Long chatId, Long userId) {
        Chat chat = chatRepository.findById(chatId)
            .orElseThrow(() -> new ChatNotFoundException(CHAT_NOT_FOUND + chatId));

        if (!chat.isDeletable()) {
            throw new AccessDeniedException(ACCESS_DENIED);
        }

        boolean isParticipant = chat.getParticipants().stream()
            .anyMatch(user -> user.getId().equals(userId));

        boolean isOwner = chat.getOwner() != null && chat.getOwner().getId().equals(userId);

        if (!isParticipant && !isOwner) {
            throw new AccessDeniedException("Доступ к чату запрещён: пользователь не является участником");
        }

        chatRepository.deleteById(chatId);
    }
}
