package startapp.com.core.views.users;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import java.util.List;
import java.util.stream.Collectors;
import startapp.com.core.dto.UserDto;
import startapp.com.core.service.UserService;
import startapp.com.core.views.MainLayout;
import startapp.com.core.views.helper.NavigationHelper;

/**
 * Представление со списком всех пользователей
 */
@Route(value = "users", layout = MainLayout.class)
@RouteAlias(value = "users/list", layout = MainLayout.class)
@PageTitle("Пользователи | Startapp Chat Core")
public class UsersListView extends VerticalLayout {

    private final UserService userService;

    private Grid<UserDto> usersGrid;
    private TextField searchField;
    private Div usersContainer;

    public UsersListView(UserService userService) {
        this.userService = userService;

        addClassName("app-container");
        setSpacing(false);
        setPadding(false);
        setWidthFull();

        initContent();
        loadUsers();
    }

    private void initContent() {
        add(createHeader());
        add(createFilters());
        add(createUsersTable());
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
        H2 title = new H2("Пользователи");
        title.getStyle().set("margin", "0 0 4px 0");

        Paragraph subtitle = new Paragraph("Управляйте пользователями системы");
        subtitle.getStyle().set("color", "#6b7280");
        subtitle.getStyle().set("font-size", "0.875rem");
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

    private Div createFilters() {
        Div filtersContainer = new Div();
        filtersContainer.addClassName("filters-card");
        filtersContainer.getStyle().set("background", "white");
        filtersContainer.getStyle().set("border-radius", "12px");
        filtersContainer.getStyle().set("padding", "20px");
        filtersContainer.getStyle().set("margin-bottom", "24px");
        filtersContainer.getStyle().set("box-shadow", "0 2px 8px rgba(0,0,0,0.05)");

        HorizontalLayout layout = new HorizontalLayout();
        layout.setWidthFull();
        layout.setAlignItems(com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment.CENTER);
        layout.setSpacing(true);

        searchField = new TextField();
        searchField.setPlaceholder("Поиск по имени...");
        searchField.addClassName("search-input");
        searchField.setWidth("300px");
        searchField.addValueChangeListener(e -> loadUsers());

        Div counter = new Div();
        counter.setId("user-counter");
        counter.getStyle().set("margin-left", "auto");
        counter.getStyle().set("color", "#6b7280");
        counter.getStyle().set("font-size", "0.875rem");

        layout.add(searchField, counter);
        filtersContainer.add(layout);

        return filtersContainer;
    }

    private Div createUsersTable() {
        usersContainer = new Div();
        usersContainer.addClassName("users-table-container");
        usersContainer.setWidthFull();
        usersContainer.getStyle().set("background", "white");
        usersContainer.getStyle().set("border-radius", "16px");
        usersContainer.getStyle().set("overflow", "hidden");
        usersContainer.getStyle().set("box-shadow", "0 4px 20px rgba(0,0,0,0.08)");

        usersGrid = new Grid<>(UserDto.class, false);
        usersGrid.setWidthFull();
        usersGrid.setHeight("600px");
        usersGrid.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_ROW_STRIPES);

        usersGrid.addColumn(UserDto::getId)
            .setHeader("ID")
            .setWidth("80px")
            .setSortable(true);

        usersGrid.addColumn(new ComponentRenderer<>(user -> {
            Div avatar = new Div();
            avatar.getStyle().set("width", "40px");
            avatar.getStyle().set("height", "40px");
            avatar.getStyle().set("border-radius", "50%");
            avatar.getStyle().set("background", "linear-gradient(135deg, #6366f1, #818cf8)");
            avatar.getStyle().set("display", "flex");
            avatar.getStyle().set("align-items", "center");
            avatar.getStyle().set("justify-content", "center");
            avatar.getStyle().set("color", "white");
            avatar.getStyle().set("font-weight", "bold");
            String initials = user.getName() != null && !user.getName().isEmpty()
                ? user.getName().substring(0, 1).toUpperCase()
                : "?";
            avatar.add(new Span(initials));
            return avatar;
        })).setHeader("Аватар").setWidth("100px");

        usersGrid.addColumn(UserDto::getName)
            .setHeader("Имя")
            .setWidth("200px")
            .setSortable(true);

        usersGrid.addColumn(user -> {
            Span badge = new Span((user.getAge() != null ? user.getAge() : 0) + " лет");
            int age = user.getAge() != null ? user.getAge() : 0;
            if (age < 18) {
                badge.getStyle().set("background", "#fef3c7");
                badge.getStyle().set("color", "#d97706");
            } else if (age < 35) {
                badge.getStyle().set("background", "#d1fae5");
                badge.getStyle().set("color", "#059669");
            } else {
                badge.getStyle().set("background", "#dbeafe");
                badge.getStyle().set("color", "#2563eb");
            }
            badge.getStyle().set("padding", "6px 12px");
            badge.getStyle().set("border-radius", "20px");
            badge.getStyle().set("font-size", "0.8rem");
            badge.getStyle().set("font-weight", "600");
            return badge;
        }).setHeader("Возраст").setWidth("120px");

        usersGrid.addColumn(user -> {
            HorizontalLayout actions = new HorizontalLayout();
            actions.setSpacing(false);
            actions.setPadding(false);

            Button editBtn = new Button();
            editBtn.setIcon(new Icon(VaadinIcon.PENCIL));
            editBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            editBtn.addClickListener(e -> NavigationHelper.navigateTo(UserFormView.class, user.getId()));

            Button deleteBtn = new Button();
            deleteBtn.setIcon(new Icon(VaadinIcon.TRASH));
            deleteBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR);
            deleteBtn.addClickListener(e -> {
                userService.deleteUser(user.getId());
                loadUsers();
            });

            actions.add(editBtn, deleteBtn);
            return actions;
        }).setHeader("Действия").setWidth("150px").setFlexGrow(0);

        usersGrid.setSelectionMode(Grid.SelectionMode.NONE);

        usersContainer.add(usersGrid);

        return usersContainer;
    }

    private void loadUsers() {
        List<UserDto> allUsers = userService.getAllUsers();

        List<UserDto> filteredUsers = allUsers.stream()
            .filter(user -> {
                if (searchField.getValue() == null || searchField.getValue().isEmpty()) {
                    return true;
                }
                String search = searchField.getValue().toLowerCase();
                return user.getName() != null && user.getName().toLowerCase().contains(search);
            })
            .collect(Collectors.toList());

        usersGrid.setItems(filteredUsers);

        Div counter = new Div();
        counter.setText(filteredUsers.size() + " из " + allUsers.size() + " пользователей");
        counter.getStyle().set("color", "#6b7280");
        counter.getStyle().set("font-size", "0.875rem");

        if (filteredUsers.isEmpty()) {
            showEmptyState();
        } else {
            hideEmptyState();
        }
    }

    private void showEmptyState() {
        usersGrid.setVisible(false);

        Div emptyState = new Div();
        emptyState.getStyle().set("text-align", "center");
        emptyState.getStyle().set("padding", "60px 20px");
        emptyState.getStyle().set("color", "#6b7280");

        Div icon = new Div("🔍");
        icon.getStyle().set("font-size", "4rem");
        icon.getStyle().set("opacity", "0.5");
        icon.getStyle().set("margin-bottom", "16px");

        emptyState.add(icon);
        emptyState.add(new H3("Ничего не найдено"));
        emptyState.add(new Paragraph("Попробуйте изменить параметры поиска"));

        usersContainer.add(emptyState);
    }

    private void hideEmptyState() {
        usersGrid.setVisible(true);
        usersContainer.getChildren()
            .filter(c -> c instanceof Div && ((Div) c).getStyle().get("text-align") != null)
            .forEach(usersContainer::remove);
    }
}