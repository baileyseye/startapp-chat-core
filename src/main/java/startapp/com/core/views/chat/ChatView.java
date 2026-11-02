package startapp.com.core.views.chat;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import jakarta.servlet.http.HttpSession;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import startapp.com.core.dto.ChatDto;
import startapp.com.core.dto.ChatUserDto;
import startapp.com.core.dto.MessageDto;
import startapp.com.core.dto.SearchResultDto;
import startapp.com.core.model.Chat;
import startapp.com.core.service.AuthService;
import startapp.com.core.service.ChatService;
import startapp.com.core.service.UserService;
import startapp.com.core.views.MainLayout;

@Route(value = "chat", layout = MainLayout.class)
public class ChatView extends VerticalLayout implements BeforeEnterObserver {

    private static final int MESSAGE_PAGE_SIZE = 50;

    private final ChatService chatService;
    private final UserService userService;
    private final AuthService authService;
    private final HttpSession session;

    private final Div rootContainer;
    private VerticalLayout chatsListPanel;
    private VerticalLayout chatsContainer;
    private HorizontalLayout chatMainPanel;
    private VerticalLayout chatArea;
    private HorizontalLayout chatHeader;
    private VerticalLayout messagesContainer;
    private TextField messageInput;
    private Button sendButton;
    private Div infoPanel;
    private VerticalLayout infoContent;

    private Long currentChatId;
    private Long currentUserId;
    private String currentUsername;
    private Long selectedContactId;

    private Button backButton;
    private Span chatTitle;
    private Button infoButton;
    private ChatDto currentChatInfo;

    private List<ChatDto> chats;
    private List<MessageDto> allMessages;

    private HorizontalLayout inputArea;

    private Span currentUserLabel;

    private TextField searchInput;
    private VerticalLayout searchResultsContainer;

    public ChatView(ChatService chatService, UserService userService, AuthService authService, HttpSession session) {
        this.chatService = chatService;
        this.userService = userService;
        this.authService = authService;
        this.session = session;

        // Полностью убираем отступы и границы
        setMargin(false);
        setPadding(false);
        setSpacing(false);
        setSizeFull();

        rootContainer = new Div();
        rootContainer.addClassName("chat-view-container");
        rootContainer.setSizeFull();

        createLayout();
    }

    private void createLayout() {
        chatsListPanel = createChatsListPanel();
        chatMainPanel = createChatMainPanel();

        rootContainer.addClassName("chat-view-container");
        rootContainer.add(chatsListPanel, chatMainPanel);
        add(rootContainer);

        // Убираем отступы у корневого контейнера
        setMargin(false);
        setPadding(false);
        setSpacing(false);
    }

    private VerticalLayout createChatsListPanel() {
        VerticalLayout panel = new VerticalLayout();
        panel.addClassName("chat-list-panel");
        panel.setWidth("280px");
        panel.setHeightFull();
        panel.setPadding(false);
        panel.setSpacing(false);
        panel.getStyle().set("background", "#fff").set("border-right", "1px solid #e0e0e0").set("flex-shrink", "0");

        HorizontalLayout header = new HorizontalLayout();
        header.addClassName("chat-header");
        header.setWidthFull();
        header.setHeight("60px");
        header.setPadding(true);
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.getStyle().set("background", "linear-gradient(135deg, #667eea, #764ba2)");

        Icon chatIcon = new Icon(VaadinIcon.CHAT);
        chatIcon.getStyle().set("color", "#fff").set("font-size", "1.5rem");

        Span title = new Span("Чаты");
        title.getStyle().set("color", "#fff").set("font-size", "1.2rem").set("font-weight", "600");

        header.add(chatIcon, title);
        panel.add(header);

        searchInput = new TextField();
        searchInput.addClassName("chat-search-input");
        searchInput.setPlaceholder("");
        searchInput.setWidthFull();
        searchInput.getStyle().set("margin", "0 12px 8px 12px").set("border", "none").set("background", "#f5f5f5");
        searchInput.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
        searchInput.setValueChangeMode(ValueChangeMode.LAZY);
        searchInput.addValueChangeListener(e -> handleSearch(e.getValue()));
        panel.add(searchInput);

        searchResultsContainer = new VerticalLayout();
        searchResultsContainer.setWidthFull();
        searchResultsContainer.setHeight("0px");
        searchResultsContainer.setPadding(false);
        searchResultsContainer.setSpacing(false);
        searchResultsContainer.getStyle().set("overflow-y", "auto").set("display", "none");
        searchResultsContainer.getElement().setAttribute("id", "searchResultsContainer_" + currentUserId);
        panel.add(searchResultsContainer);

        chatsContainer = new VerticalLayout();
        chatsContainer.setWidthFull();
        chatsContainer.setHeightFull();
        chatsContainer.setPadding(false);
        chatsContainer.setSpacing(false);
        chatsContainer.getStyle().set("overflow-y", "auto");

        panel.add(chatsContainer);

        return panel;
    }

    private HorizontalLayout createChatMainPanel() {
        HorizontalLayout panel = new HorizontalLayout();
        panel.addClassName("chat-main-panel");
        panel.setWidthFull();
        panel.setHeightFull();
        panel.setPadding(false);
        panel.setSpacing(false);
        panel.getStyle().set("flex-grow", "1").set("position", "relative");

        chatArea = new VerticalLayout();
        chatArea.addClassName("chat-area");
        chatArea.setSizeFull();
        chatArea.setPadding(false);
        chatArea.setSpacing(false);

        chatHeader = new HorizontalLayout();
        chatHeader.setWidthFull();
        chatHeader.setHeight("60px");
        chatHeader.setPadding(true);
        chatHeader.setAlignItems(FlexComponent.Alignment.CENTER);
        chatHeader.setJustifyContentMode(JustifyContentMode.BETWEEN);
        chatHeader.getStyle().set("background", "linear-gradient(135deg, #667eea, #764ba2)");

        // Кнопка "Назад" (слева)
        backButton = new Button(new Icon(VaadinIcon.CARET_LEFT));
        backButton.addClassName("chat-back-button");
        backButton.setVisible(false);
        backButton.addClickListener(e -> deselectChat());

        // Заголовок (по центру)
        chatTitle = new Span("Выберите чат");
        chatTitle.getStyle().set("color", "#fff").set("font-size", "1.1rem").set("font-weight", "600");

        // Кнопка информации (справа)
        infoButton = new Button(new Icon(VaadinIcon.INFO_CIRCLE));
        infoButton.addClassName("chat-info-button");
        infoButton.getStyle().set("background", "transparent").set("color", "#fff").set("border", "none")
            .set("font-size", "1.4rem").set("line-height", "1");
        infoButton.setVisible(false);
        infoButton.addClickListener(e -> toggleInfoPanel());

        chatHeader.add(backButton, chatTitle, infoButton);

        chatArea.add(chatHeader);

        messagesContainer = new VerticalLayout();
        messagesContainer.addClassName("chat-messages-container");
        messagesContainer.setWidthFull();
        messagesContainer.setHeightFull();
        messagesContainer.setPadding(true);
        messagesContainer.setSpacing(true);
        messagesContainer.getStyle().set("overflow-y", "auto")
            .set("background", "linear-gradient(180deg, #a8c0ff 0%, #cfd9df 100%)");

        Span emptyMessage = new Span("Выберите чат, чтобы начать общение");
        emptyMessage.addClassName("empty-chat-message");
        messagesContainer.add(emptyMessage);

        chatArea.add(messagesContainer);

        // Контейнер для области ввода (скрыт по умолчанию)
        inputArea = new HorizontalLayout();
        inputArea.addClassName("chat-input-area");
        inputArea.setWidthFull();
        inputArea.setHeight("70px");
        inputArea.setPadding(true);
        inputArea.setAlignItems(FlexComponent.Alignment.END);
        inputArea.getStyle().set("background", "#fff").set("border-top", "1px solid #e0e0e0");
        inputArea.setVisible(false); // Скрыто пока чат не выбран

        messageInput = new TextField();
        messageInput.setWidthFull();
        messageInput.setPlaceholder("Напишите сообщение...");
        messageInput.setValueChangeMode(ValueChangeMode.LAZY);
        messageInput.getStyle().set("border", "none");

        sendButton = new Button(new Icon(VaadinIcon.CHECK));
        sendButton.getStyle().set("background", "linear-gradient(135deg, #667eea, #764ba2)").set("color", "#fff")
            .set("border", "none")
            .set("border-radius", "50%").set("width", "48px").set("height", "48px").set("cursor", "pointer");
        sendButton.addClickListener(e -> sendMessage());

        // Отправка сообщения по Enter
        messageInput.addKeyPressListener(Key.ENTER, e -> sendMessage());

        inputArea.add(messageInput, sendButton);
        chatArea.add(inputArea);

        panel.add(chatArea);

        return panel;
    }

    private Div createInfoPanel() {
        Div panel = new Div();
        panel.addClassName("chat-info-overlay");
        panel.getStyle().set("position", "absolute")
            .set("top", "0").set("left", "0").set("right", "0").set("bottom", "0")
            .set("background", "rgba(0, 0, 0, 0.5)")
            .set("display", "flex").set("align-items", "center").set("justify-content", "center")
            .set("z-index", "100");
        panel.addClickListener(e -> closeInfoPanel());

        Div dialog = new Div();
        dialog.addClassName("chat-info-dialog");
        dialog.setWidth("320px");
        dialog.getStyle().set("background", "#fff")
            .set("border-radius", "12px")
            .set("box-shadow", "0 4px 20px rgba(0,0,0,0.3)")
            .set("padding", "0");

        HorizontalLayout header = new HorizontalLayout();
        header.addClassName("chat-info-header");
        header.setWidthFull();
        header.setHeight("50px");
        header.setPadding(false);
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.setJustifyContentMode(JustifyContentMode.BETWEEN);
        header.getStyle().set("background", "#5682ab")
            .set("border-top-left-radius", "12px")
            .set("border-top-right-radius", "12px")
            .set("padding-left", "16px")
            .set("padding-right", "16px");

        Span title = new Span("Информация");
        title.getStyle().set("color", "#fff").set("font-size", "1rem").set("font-weight", "600");

        Button closeBtn = new Button(new Icon(VaadinIcon.CLOSE));
        closeBtn.getStyle().set("background", "transparent").set("color", "#fff")
            .set("border", "none").set("font-size", "1.2rem")
            .set("cursor", "pointer");
        closeBtn.addClickListener(e -> closeInfoPanel());

        header.add(title, closeBtn);

        infoContent = new VerticalLayout();
        infoContent.setWidthFull();
        infoContent.setHeight("auto");
        infoContent.setMaxHeight("400px");
        infoContent.setPadding(true);
        infoContent.setSpacing(true);
        infoContent.setAlignItems(FlexComponent.Alignment.CENTER);
        infoContent.getStyle().set("overflow-y", "auto");

        dialog.add(header, infoContent);
        panel.add(dialog);

        return panel;
    }

    private void handleSearch(String query) {
        if (query == null || query.isBlank()) {
            searchResultsContainer.getElement().getStyle().set("display", "none");
            searchResultsContainer.setHeight("0px");
            renderChatsList();
            return;
        }

        String encodedQuery = java.net.URLEncoder.encode(query, java.nio.charset.StandardCharsets.UTF_8);
        String url = "/api/chats/search?userId=" + currentUserId + "&query=" + encodedQuery;

        // Используем JavaScript для выполнения fetch запроса и передачи данных обратно
        getElement().executeJs("""
                fetch($0)
                    .then(response => response.json())
                    .then(data => {
                        this.$server.onSearchResultsReceived(JSON.stringify(data));
                    })
                    .catch(error => console.error('Search error:', error));
            """, url);
    }

    @com.vaadin.flow.component.ClientCallable
    private void onSearchResultsReceived(String jsonData) {
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            java.util.List<SearchResultDto> results = mapper.readValue(
                jsonData,
                mapper.getTypeFactory().constructCollectionType(java.util.List.class, SearchResultDto.class)
            );
            renderSearchResults(results);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void renderSearchResults(java.util.List<SearchResultDto> results) {
        searchResultsContainer.getElement().removeAllChildren();

        boolean hasChats = false;
        boolean hasUsers = false;

        // Проверяем наличие чатов и пользователей
        for (SearchResultDto r : results) {
            if ("chat".equals(r.getType())) {
                hasChats = true;
            }
            if ("user".equals(r.getType())) {
                hasUsers = true;
            }
        }

        // Сначала рендерим существующие чаты
        if (hasChats) {
            Span sectionTitle = new Span("Существующие чаты");
            sectionTitle.getStyle().set("font-size", "0.75rem").set("color", "#aaa").set("padding", "8px 12px")
                .set("font-weight", "600");
            searchResultsContainer.add(sectionTitle);

            for (SearchResultDto r : results) {
                if ("chat".equals(r.getType())) {
                    searchResultsContainer.add(createSearchChatItem(r.getId(), r.getName(), r.getLastMessage()));
                }
            }

            // Разделитель
            Div separator = new Div();
            separator.getStyle().set("border-top", "1px dashed #444").set("margin", "8px 12px");
            searchResultsContainer.add(separator);
        }

        // Затем рендерим пользователей
        if (hasUsers) {
            Span sectionTitle = new Span("Новые контакты");
            sectionTitle.getStyle().set("font-size", "0.75rem").set("color", "#aaa").set("padding", "8px 12px")
                .set("font-weight", "600");
            searchResultsContainer.add(sectionTitle);

            for (SearchResultDto r : results) {
                if ("user".equals(r.getType())) {
                    searchResultsContainer.add(createSearchUserItem(r.getId(), r.getName()));
                }
            }
        }

        searchResultsContainer.getElement().getStyle().set("display", "block");
        long count = searchResultsContainer.getChildren().count();
        int height = (int) Math.max(count * 50 + 20, 100);
        searchResultsContainer.setHeight(height + "px");
    }

    private com.vaadin.flow.component.Component createSearchChatItem(long chatId, String title, String lastMessage) {
        HorizontalLayout item = new HorizontalLayout();
        item.setWidthFull();
        item.setPadding(true);
        item.setSpacing(true);
        item.setAlignItems(com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment.CENTER);
        item.getStyle().set("cursor", "pointer").set("border-bottom", "1px solid #333").set("background", "#2a2a2a");

        item.addClickListener(e -> {
            searchResultsContainer.getElement().getStyle().set("display", "none");
            searchInput.setValue("");
            selectExistingChat(chatId);
        });

        Div avatar = new Div();
        avatar.setWidth("40px");
        avatar.setHeight("40px");
        avatar.getStyle().set("flex-shrink", "0")
            .set("background", "linear-gradient(135deg, #667eea, #764ba2)")
            .set("border-radius", "50%")
            .set("display", "flex").set("align-items", "center").set("justify-content", "center")
            .set("color", "#fff").set("font-weight", "600").set("font-size", "1rem")
            .set("overflow", "hidden");

        String initials = title != null && !title.isEmpty() ? title.substring(0, 1).toUpperCase() : "?";
        Span span = new Span(initials);
        span.getStyle().set("display", "block");
        avatar.add(span);

        VerticalLayout info = new VerticalLayout();
        info.setWidthFull();
        info.setPadding(false);
        info.setSpacing(false);

        H4 chatTitle = new H4(title != null ? title : "Без названия");
        chatTitle.getStyle().set("margin", "0").set("font-size", "0.9rem").set("color", "#eee");

        Paragraph lastMsg = new Paragraph(lastMessage != null ? lastMessage : "Нет сообщений");
        lastMsg.getStyle().set("margin", "0").set("font-size", "0.75rem").set("color", "#aaa");

        info.add(chatTitle, lastMsg);
        item.add(avatar, info);

        return item;
    }

    private com.vaadin.flow.component.Component createSearchUserItem(long userId, String userName) {
        HorizontalLayout item = new HorizontalLayout();
        item.setWidthFull();
        item.setPadding(true);
        item.setSpacing(true);
        item.setAlignItems(com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment.CENTER);
        item.getStyle().set("cursor", "pointer").set("border-bottom", "1px solid #333").set("background", "#222");

        item.addClickListener(e -> {
            searchResultsContainer.getElement().getStyle().set("display", "none");
            searchInput.setValue("");
            createAndSelectChat(userId, userName);
        });

        Div avatar = new Div();
        avatar.setWidth("40px");
        avatar.setHeight("40px");
        avatar.getStyle().set("flex-shrink", "0")
            .set("background", "linear-gradient(135deg, #f093fb, #f5576c)")
            .set("border-radius", "50%")
            .set("display", "flex").set("align-items", "center").set("justify-content", "center")
            .set("color", "#fff").set("font-weight", "600").set("font-size", "1rem")
            .set("overflow", "hidden");

        String initials = userName != null && !userName.isEmpty() ? userName.substring(0, 1).toUpperCase() : "?";
        Span span = new Span(initials);
        span.getStyle().set("display", "block");
        avatar.add(span);

        VerticalLayout info = new VerticalLayout();
        info.setWidthFull();
        info.setPadding(false);
        info.setSpacing(false);

        H4 userTitle = new H4(userName != null ? userName : "Неизвестный пользователь");
        userTitle.getStyle().set("margin", "0").set("font-size", "0.9rem").set("color", "#f5576c");

        Paragraph hint = new Paragraph("Нажмите, чтобы создать чат");
        hint.getStyle().set("margin", "0").set("font-size", "0.75rem").set("color", "#aaa");

        info.add(userTitle, hint);
        item.add(avatar, info);

        return item;
    }

    private void selectExistingChat(long chatId) {
        // Находим чат в списке и выбираем его
        for (ChatDto chat : chats) {
            if (chat.getId().equals(chatId)) {
                selectChat(chat);
                return;
            }
        }
        // Если чат не найден (возможно, только что создан другим пользователем), перезагружаем
        loadChats();
        for (ChatDto chat : chats) {
            if (chat.getId().equals(chatId)) {
                selectChat(chat);
                return;
            }
        }
    }

    private void createAndSelectChat(long otherUserId, String otherUserName) {
        // Создаем чат через сервис напрямую (так как мы в UI потоке)
        try {
            Chat newChat = chatService.getOrCreatePrivateChat(currentUserId, otherUserId);
            ChatDto newChatDto = chatService.getAllChatsForUser(currentUserId).stream()
                .filter(c -> c.getId().equals(newChat.getId()))
                .findFirst()
                .orElse(null);

            if (newChatDto != null) {
                loadChats();
                for (ChatDto chat : chats) {
                    if (chat.getId().equals(newChatDto.getId())) {
                        selectChat(chat);
                        return;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void toggleInfoPanel() {
        if (infoPanel == null) {
            infoPanel = createInfoPanel();
            add(infoPanel);
        }

        boolean isOpening = infoPanel.getElement().getStyle().get("display") == null ||
                            "none".equals(infoPanel.getElement().getStyle().get("display"));

        if (isOpening) {
            // Показываем уже загруженные данные (если они есть)
            if (infoContent != null && currentChatInfo != null) {
                infoContent.getElement().removeAllChildren();
                loadChatInfo(currentChatInfo);
            }
            infoPanel.getElement().getStyle().set("display", "flex");
        } else {
            infoPanel.getElement().getStyle().set("display", "none");
        }
    }

    private void closeInfoPanel() {
        if (infoPanel != null) {
            infoPanel.getElement().getStyle().set("display", "none");
        }
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        currentUserId = (Long) session.getAttribute("userId");
        currentUsername = (String) session.getAttribute("username");

        if (currentUserId == null) {
            event.forwardTo("login");
            return;
        }

        loadChats();
    }

    private void loadChats() {
        // Создаем чат "Избранное" для пользователя, если его еще нет
        chatService.getOrCreateFavoritesChat(currentUserId);

        // Затем загружаем все чаты (включая "Избранное")
        chats = new ArrayList<>(chatService.getAllChatsForUser(currentUserId));
        // Сортируем чаты по времени последнего сообщения (сначала свежие)
        chats.sort((c1, c2) -> {
            var t1 = c1.getLastMessageTime();
            var t2 = c2.getLastMessageTime();
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
        });
        renderChatsList();
    }

    private void renderChatsList() {
        if (chatsContainer != null) {
            chatsContainer.getElement().removeAllChildren();
            for (ChatDto chat : chats) {
                chatsContainer.add(createChatItem(chat));
            }
        }
    }

    private Component createChatItem(ChatDto chat) {
        HorizontalLayout item = new HorizontalLayout();
        item.addClassName("chat-item");
        item.setWidthFull();
        item.setPadding(true);
        item.setSpacing(true);
        item.setAlignItems(FlexComponent.Alignment.CENTER);
        item.getStyle().set("cursor", "pointer").set("border-bottom", "1px solid #f0f0f0");

        item.addClickListener(e -> selectChat(chat));

        Div avatar = new Div();
        avatar.addClassName("chat-avatar");
        avatar.setWidth("50px");
        avatar.setHeight("50px");
        avatar.getStyle().set("background", "linear-gradient(135deg, #667eea, #764ba2)")
            .set("border-radius", "50%")
            .set("display", "flex").set("align-items", "center").set("justify-content", "center")
            .set("color", "#fff").set("font-weight", "600").set("font-size", "1.2rem");

        String initials = extractInitials(chat.getTitle());
        avatar.add(new Span(initials));

        // Добавляем иконку для специальных чатов
        if ("favorites".equals(chat.getType())) {
            avatar.getStyle().set("background", "linear-gradient(135deg, #f093fb, #f5576c)");
            avatar.removeAll();
            avatar.add(new Icon(VaadinIcon.STAR));
        } else if ("system".equals(chat.getType())) {
            avatar.getStyle().set("background", "linear-gradient(135deg, #4facfe, #00f2fe)");
            avatar.removeAll();
            avatar.add(new Icon(VaadinIcon.COG));
        }

        VerticalLayout chatInfo = new VerticalLayout();
        chatInfo.setWidthFull();
        chatInfo.setPadding(false);
        chatInfo.setSpacing(false);

        HorizontalLayout titleRow = new HorizontalLayout();
        titleRow.setWidthFull();
        titleRow.setJustifyContentMode(JustifyContentMode.BETWEEN);
        titleRow.setAlignItems(FlexComponent.Alignment.CENTER);

        H4 chatTitle = new H4(chat.getTitle());
        chatTitle.addClassName("chat-title-text");
        chatTitle.getStyle().set("margin", "0").set("font-size", "1rem");

        if ("favorites".equals(chat.getType())) {
            chatTitle.getStyle().set("color", "#f5576c").set("font-weight", "700");
        }

        Span time = new Span(formatTime(chat.getLastMessageTime()));
        time.addClassName("chat-time-text");
        time.getStyle().set("font-size", "0.75rem").set("color", "#888");

        titleRow.add(chatTitle, time);

        Paragraph lastMessage = new Paragraph(chat.getLastMessage() != null ? chat.getLastMessage() : "Нет сообщений");
        lastMessage.addClassName("chat-last-message-text");
        lastMessage.getStyle().set("margin", "0").set("font-size", "0.85rem").set("color", "#888")
            .set("white-space", "nowrap").set("overflow", "hidden").set("text-overflow", "ellipsis");

        chatInfo.add(titleRow, lastMessage);
        item.add(avatar, chatInfo);

        return item;
    }

    private void selectChat(ChatDto chat) {
        currentChatId = chat.getId();
        selectedContactId = null;
        currentChatInfo = chat; // Сохраняем данные чата

        // Обновляем заголовок
        chatTitle.setText(chat.getTitle());

        // Показываем кнопку "Назад" когда выбран чат
        backButton.setVisible(true);

        // Обновляем кнопку информации
        infoButton.setVisible(!"favorites".equals(chat.getType()));

        allMessages = new ArrayList<>();

        // Показываем поле ввода и скрываем пустое сообщение
        inputArea.setVisible(true);
        loadMessagesPage();
    }

    private void deselectChat() {
        currentChatId = null;
        selectedContactId = null;
        currentChatInfo = null; // Сбрасываем данные чата
        allMessages = new ArrayList<>();

        // Скрываем кнопку "Назад"
        backButton.setVisible(false);

        // Возвращаем заголовок "Выберите чат"
        chatTitle.setText("Выберите чат");

        // Скрываем кнопку информации
        infoButton.setVisible(false);

        // Скрываем поле ввода
        inputArea.setVisible(false);

        // Очищаем сообщения и показываем пустое состояние
        messagesContainer.getElement().removeAllChildren();
        Span emptyMessage = new Span("Выберите чат, чтобы начать общение");
        emptyMessage.addClassName("empty-chat-message");
        messagesContainer.add(emptyMessage);
    }

    private void loadChatInfo(ChatDto chat) {
        infoContent.removeAll();
        List<ChatUserDto> participants = chat.getParticipants();

        if (participants == null || participants.isEmpty()) {
            infoContent.add(new Span("Нет участников"));
            return;
        }

        // Ищем участника, который не是当前 пользователь
        for (ChatUserDto participant : participants) {
            if (!participant.getId().equals(currentUserId)) {
                selectedContactId = participant.getId();
                renderUserInfo(participant);
                return;
            }
        }

        infoContent.add(new Span("Системный чат"));
    }

    private void renderUserInfo(ChatUserDto user) {
        infoContent.removeAll();

        Div avatar = new Div();
        avatar.setWidth("80px");
        avatar.setHeight("80px");
        avatar.getStyle().set("background", "linear-gradient(135deg, #667eea, #764ba2)")
            .set("border-radius", "50%").set("display", "flex").set("align-items", "center")
            .set("justify-content", "center").set("color", "#fff").set("font-size", "2rem")
            .set("font-weight", "bold").set("margin", "0 auto 20px auto");
        avatar.add(new Span(user.getName().substring(0, 1).toUpperCase()));

        infoContent.add(avatar);

        H4 name = new H4(user.getName());
        name.addClassName("chat-info-content");
        name.getStyle().set("text-align", "center").set("margin", "0 0 10px 0");

        Div details = new Div();
        details.setWidthFull();

        Div detailRow1 = new Div();
        detailRow1.addClassName("chat-detail-row");
        detailRow1.getStyle().set("padding", "8px 0").set("border-bottom", "1px solid #f0f0f0");
        detailRow1.add(new Span("ID чата: " + currentChatId));

        Div detailRow2 = new Div();
        detailRow2.addClassName("chat-detail-row");
        detailRow2.getStyle().set("padding", "8px 0").set("border-bottom", "1px solid #f0f0f0");
        detailRow2.add(new Span("ID пользователя: " + user.getId()));

        Div detailRow3 = new Div();
        detailRow3.addClassName("chat-detail-row");
        detailRow3.getStyle().set("padding", "8px 0");
        detailRow3.add(new Span("Статус: " + (user.getStatus() != null ? user.getStatus() : "N/A")));

        details.add(detailRow1, detailRow2, detailRow3);

        infoContent.add(name, details);
    }

    private void loadMessagesPage() {
        if (allMessages.isEmpty()) {
            try {
                var pageDto = chatService.getMessagesPaged(currentChatId, 0, MESSAGE_PAGE_SIZE, currentUserId);
                allMessages.addAll(pageDto.getMessages());
                renderMessages();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void renderMessages() {
        messagesContainer.getElement().removeAllChildren();

        if (allMessages.isEmpty()) {
            Span noMessages = new Span("Нет сообщений");
            noMessages.getStyle().set("color", "#fff").set("text-align", "center");
            messagesContainer.add(noMessages);
            return;
        }

        for (MessageDto msg : allMessages) {
            messagesContainer.add(createMessageBubble(msg));
        }

        messagesContainer.getElement().executeJs("this.scrollTop = this.scrollHeight;");
    }

    private Component createMessageBubble(MessageDto msg) {
        boolean isMine = msg.getSenderId().equals(currentUserId);
        String direction = msg.getDirection() != null ? msg.getDirection() : (isMine ? "outgoing" : "incoming");

        HorizontalLayout bubble = new HorizontalLayout();
        bubble.setWidthFull();
        bubble.setPadding(false);
        bubble.setSpacing(false);
        bubble.setJustifyContentMode(isMine ? JustifyContentMode.END : JustifyContentMode.START);

        Div content = new Div();
        content.setMaxWidth("70%");
        content.addClassName(isMine ? "chat-bubble-mine" : "chat-bubble-theirs");
        content.getStyle().set("background", isMine ? "#dcf8c6" : "#fff")
            .set("border-radius", "8px").set("padding", "8px 12px")
            .set("box-shadow", "0 1px 2px rgba(0,0,0,0.1)");

        Span text = new Span(msg.getContent());
        text.getStyle().set("word-break", "break-word");

        Span time = new Span(formatMessageTime(msg.getCreatedAt()));
        time.getStyle().set("font-size", "0.7rem").set("color", "#888").set("display", "block")
            .set("text-align", "right").set("margin-top", "4px");

        content.add(text, time);
        bubble.add(content);

        return bubble;
    }

    private void sendMessage() {
        String content = messageInput.getValue().trim();
        if (content.isEmpty() || currentChatId == null) {
            return;
        }

        try {
            MessageDto newMessage = chatService.sendMessage(currentChatId, currentUserId, content);
            allMessages.add(newMessage);
            messageInput.setValue("");
            renderMessages();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String extractInitials(String name) {
        if (name == null || name.isEmpty()) {
            return "?";
        }
        String[] parts = name.split("[& ]+");
        StringBuilder initials = new StringBuilder();
        for (String part : parts) {
            if (!part.isEmpty()) {
                initials.append(part.charAt(0));
                if (initials.length() >= 2) {
                    break;
                }
            }
        }
        return initials.toString().toUpperCase();
    }

    private String formatTime(java.time.LocalDateTime time) {
        if (time == null) {
            return "";
        }
        return time.format(DateTimeFormatter.ofPattern("HH:mm"));
    }

    private String formatMessageTime(java.time.LocalDateTime time) {
        if (time == null) {
            return "";
        }
        return time.format(DateTimeFormatter.ofPattern("HH:mm"));
    }
}
