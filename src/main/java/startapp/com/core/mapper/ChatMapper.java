package startapp.com.core.mapper;

import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import startapp.com.core.dto.ChatDto;
import startapp.com.core.dto.ChatUserDto;
import startapp.com.core.dto.MessageDto;
import startapp.com.core.model.Chat;
import startapp.com.core.model.Message;
import startapp.com.core.model.User;

@Mapper(componentModel = "spring")
public interface ChatMapper {

    @Mapping(target = "participants", source = "chat.participants")
    @Mapping(target = "lastMessage", source = "chat.lastMessage")
    @Mapping(target = "lastMessageTime", source = "chat.lastMessageTime")
    @Mapping(target = "ownerId", source = "chat.owner.id")
    ChatDto toDto(Chat chat);

    Chat toEntity(ChatDto dto);

    default List<ChatDto> toDtoList(List<Chat> chats) {
        return chats.stream()
            .map(this::toDto)
            .toList();
    }

    /**
     * Преобразует Chat в ChatDto с динамическим заголовком для текущего пользователя.
     * Для приватных чатов показывает имя собеседника (другого участника).
     */
    default ChatDto toDtoWithUser(Chat chat, Long currentUserId) {
        ChatDto dto = toDto(chat);

        if ("private".equals(chat.getType()) && currentUserId != null) {
            String dynamicTitle = getOtherParticipantName(chat, currentUserId);

            if (dynamicTitle != null) {
                dto.setTitle(dynamicTitle);
            }
        }

        return dto;
    }

    /**
     * Преобразует список чатов в DTO с динамическими заголовками.
     */
    default List<ChatDto> toDtoListWithUser(List<Chat> chats, Long currentUserId) {
        if (currentUserId == null) {
            return toDtoList(chats);
        }

        return chats.stream()
            .map(chat -> toDtoWithUser(chat, currentUserId))
            .toList();
    }

    /**
     * Возвращает имя другого участника приватного чата (не текущего пользователя).
     */
    private String getOtherParticipantName(Chat chat, Long currentUserId) {
        return chat.getParticipants().stream()
            .filter(user -> !user.getId().equals(currentUserId))
            .findFirst()
            .map(User::getName)
            .orElse(chat.getTitle());
    }

    @Mapping(target = "senderId", source = "message.sender.id")
    @Mapping(target = "senderName", source = "message.sender.name")
    MessageDto toMessageDto(Message message);

    default MessageDto toMessageDto(Message message, Long currentUserId) {
        MessageDto dto = toMessageDto(message);

        if (currentUserId != null) {
            dto.setDirection(message.getSender().getId().equals(currentUserId) ? "outgoing" : "incoming");
        }

        return dto;
    }

    default List<MessageDto> toMessageDtoList(List<Message> messages) {
        return messages.stream()
            .map(this::toMessageDto)
            .toList();
    }

    default List<MessageDto> toMessageDtoList(List<Message> messages, Long currentUserId) {
        if (currentUserId == null) {
            return toMessageDtoList(messages);
        }

        return messages.stream()
            .map(m -> toMessageDto(m, currentUserId))
            .toList();
    }

    default ChatUserDto toChatUserDto(User user) {
        if (user == null) {
            return null;
        }

        return ChatUserDto.builder()
            .id(user.getId())
            .name(user.getName())
            .isOnline(true)
            .status("online")
            .build();
    }

    default List<ChatUserDto> toChatUserDtoList(List<User> users) {
        if (users == null) {
            return List.of();
        }

        return users.stream()
            .map(this::toChatUserDto)
            .toList();
    }
}
