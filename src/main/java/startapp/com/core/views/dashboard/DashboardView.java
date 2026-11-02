package startapp.com.core.views.dashboard;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import startapp.com.core.dto.UserDto;
import startapp.com.core.service.UserService;
import startapp.com.core.views.MainLayout;
import startapp.com.core.views.helper.NavigationHelper;
import startapp.com.core.views.users.UserFormView;
import startapp.com.core.views.users.UsersListView;

/**
 * Главная панель управления (Dashboard) со статистикой и быстрыми действиями
 */
@Route(value = "dashboard", layout = MainLayout.class)
@PageTitle("Dashboard | Startapp Chat Core")
public class DashboardView extends VerticalLayout implements BeforeEnterObserver {

    private final UserService userService;
    private final HttpSession session;

    public DashboardView(UserService userService, HttpSession session) {
        this.userService = userService;
        this.session = session;

        addClassName("app-container");
        setSpacing(false);
        setPadding(false);
        setWidthFull();

        initContent();
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (session.getAttribute("userId") == null) {
            event.forwardTo("login");
        }
    }

    private void initContent() {
        add(createHeader());
        add(createStatsSection());
        add(createRecentUsersSection());
    }

    private Div createHeader() {
        Div header = new Div();
        header.addClassName("card");
        header.setWidthFull();
        header.getStyle().set("margin-bottom", "24px");

        HorizontalLayout layout = new HorizontalLayout();
        layout.setWidthFull();
        layout.setAlignItems(com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment.CENTER);
        layout.setJustifyContentMode(com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode.BETWEEN);

        Div titleSection = new Div();
        H1 title = new H1("Добро пожаловать");
        title.getStyle().set("margin", "0 0 4px 0");

        Paragraph subtitle = new Paragraph("Управляйте пользователями вашей системы");
        subtitle.getStyle().set("color", "#6b7280");
        subtitle.getStyle().set("margin", "0");

        titleSection.add(title, subtitle);

        Button addButton = new Button("Добавить пользователя");
        addButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addButton.setIcon(new Icon(VaadinIcon.USER));
        addButton.addClickListener(e -> NavigationHelper.navigateTo(UserFormView.class));

        layout.add(titleSection, addButton);
        header.add(layout);

        return header;
    }

    private Div createStatsSection() {
        Div statsContainer = new Div();
        statsContainer.addClassName("stats-grid");
        statsContainer.setWidthFull();

        List<UserDto> users = userService.getAllUsers();

        // Всего пользователей
        statsContainer.add(createStatCard(
            "Всего пользователей",
            String.valueOf(users.size()),
            VaadinIcon.USERS,
            "#6366f1"
        ));

        // Новые сегодня (условно - ID = 1)
        long newToday = users.stream()
            .filter(u -> u.getId() != null && u.getId() == 1)
            .count();
        statsContainer.add(createStatCard(
            "Новых сегодня",
            String.valueOf(newToday),
            VaadinIcon.CALENDAR,
            "#10b981"
        ));

        // Средний возраст
        double avgAge = users.stream()
            .mapToInt(u -> u.getAge() != null ? u.getAge() : 0)
            .average()
            .orElse(0);
        statsContainer.add(createStatCard(
            "Средний возраст",
            String.format("%.0f", avgAge),
            VaadinIcon.CLOCK,
            "#f59e0b"
        ));

        // Активных (все, у кого возраст > 18)
        long activeUsers = users.stream()
            .filter(u -> u.getAge() != null && u.getAge() > 18)
            .count();
        statsContainer.add(createStatCard(
            "Активных пользователей",
            String.valueOf(activeUsers),
            VaadinIcon.USER_CHECK,
            "#06b6d4"
        ));

        return statsContainer;
    }

    private Div createStatCard(String label, String value, VaadinIcon icon, String color) {
        Div card = new Div();
        card.addClassName("stat-card");
        card.getStyle()
            .set("background", String.format("linear-gradient(135deg, %s, %s)", color, adjustColor(color, -20)));

        Icon vaadinIcon = new Icon(icon);
        vaadinIcon.getStyle().set("font-size", "2.5rem");
        vaadinIcon.getStyle().set("opacity", "0.8");

        Div valueDiv = new Div(value);
        valueDiv.getStyle().set("font-size", "3rem");
        valueDiv.getStyle().set("font-weight", "700");
        valueDiv.getStyle().set("margin", "0");
        valueDiv.getStyle().set("line-height", "1");

        Div labelDiv = new Div(label);
        labelDiv.getStyle().set("font-size", "0.9rem");
        labelDiv.getStyle().set("opacity", "0.9");
        labelDiv.getStyle().set("margin-top", "8px");
        labelDiv.getStyle().set("text-transform", "uppercase");
        labelDiv.getStyle().set("letter-spacing", "1px");

        card.add(vaadinIcon, valueDiv, labelDiv);

        return card;
    }

    private String adjustColor(String hex, int percent) {
        try {
            String cleanHex = hex.replace("#", "");
            int r = Integer.parseInt(cleanHex.substring(0, 2), 16);
            int g = Integer.parseInt(cleanHex.substring(2, 4), 16);
            int b = Integer.parseInt(cleanHex.substring(4, 6), 16);

            r = Math.max(0, Math.min(255, r + percent));
            g = Math.max(0, Math.min(255, g + percent));
            b = Math.max(0, Math.min(255, b + percent));

            return String.format("#%02x%02x%02x", r, g, b);
        } catch (Exception e) {
            return hex;
        }
    }

    private Div createRecentUsersSection() {
        Div recentContainer = new Div();
        recentContainer.addClassName("card");
        recentContainer.setWidthFull();

        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setAlignItems(com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment.CENTER);
        header.setJustifyContentMode(com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode.BETWEEN);

        H2 title = new H2("Последние пользователи");
        title.getStyle().set("margin", "0");
        header.add(title);

        Button viewAll = new Button("Все пользователи");
        viewAll.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        viewAll.setIcon(new Icon(VaadinIcon.ARROW_RIGHT));
        viewAll.addClickListener(e -> NavigationHelper.navigateTo(UsersListView.class));
        header.add(viewAll);

        recentContainer.add(header);

        List<UserDto> users = userService.getAllUsers();

        if (users.isEmpty()) {
            Div empty = new Div();
            empty.getStyle().set("text-align", "center");
            empty.getStyle().set("padding", "40px");
            empty.getStyle().set("color", "#6b7280");
            empty.add(new Div("👥"));
            empty.add(new Paragraph("Пока нет пользователей"));
            recentContainer.add(empty);
        } else {
            users.stream().limit(5).forEach(user -> {
                Div userCard = new Div();
                userCard.addClassName("user-card");
                userCard.getStyle().set("padding", "16px");
                userCard.getStyle().set("border-bottom", "1px solid #e5e7eb");

                HorizontalLayout userLayout = new HorizontalLayout();
                userLayout.setWidthFull();
                userLayout.setAlignItems(com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment.CENTER);
                userLayout.setSpacing(false);

                // Аватар
                Div avatar = new Div();
                avatar.getStyle().set("width", "48px");
                avatar.getStyle().set("height", "48px");
                avatar.getStyle().set("border-radius", "50%");
                avatar.getStyle().set("background", "linear-gradient(135deg, #6366f1, #818cf8)");
                avatar.getStyle().set("display", "flex");
                avatar.getStyle().set("align-items", "center");
                avatar.getStyle().set("justify-content", "center");
                avatar.getStyle().set("color", "white");
                avatar.getStyle().set("font-weight", "bold");
                avatar.getStyle().set("font-size", "1.2rem");
                String initials = user.getName() != null && !user.getName().isEmpty()
                    ? user.getName().substring(0, 1).toUpperCase()
                    : "?";
                avatar.add(new Span(initials));

                // Информация
                Div userInfo = new Div();
                userInfo.setWidthFull();

                Div name = new Div(user.getName() != null ? user.getName() : "Без имени");
                name.getStyle().set("font-weight", "600");

                Div details = new Div();
                String idStr = user.getId() != null ? String.valueOf(user.getId()) : "N/A";
                String ageStr = user.getAge() != null ? String.valueOf(user.getAge()) : "N/A";
                details.add(new Span(String.format("ID: %s", idStr)));
                details.add(new Span(" • "));
                details.add(new Span(String.format("Возраст: %s", ageStr)));
                details.getStyle().set("color", "#6b7280");
                details.getStyle().set("font-size", "0.875rem");

                userInfo.add(name, details);

                // Кнопки действий
                HorizontalLayout actions = new HorizontalLayout();
                actions.setSpacing(false);

                Button editBtn = new Button();
                editBtn.setIcon(new Icon(VaadinIcon.PENCIL));
                editBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
                if (user.getId() != null) {
                    editBtn.addClickListener(e -> NavigationHelper.navigateTo(UserFormView.class, user.getId()));
                }

                Button deleteBtn = new Button();
                deleteBtn.setIcon(new Icon(VaadinIcon.TRASH));
                deleteBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR);
                if (user.getId() != null) {
                    Long userId = user.getId();
                    deleteBtn.addClickListener(e -> {
                        userService.deleteUser(userId);
                        initContent();
                        getElement().executeJs(
                            "this.parentElement.parentElement.dispatchEvent(new CustomEvent('refresh'))");
                    });
                }

                actions.add(editBtn, deleteBtn);

                userLayout.add(avatar, userInfo, actions);
                userCard.add(userLayout);
                recentContainer.add(userCard);
            });
        }

        return recentContainer;
    }
}