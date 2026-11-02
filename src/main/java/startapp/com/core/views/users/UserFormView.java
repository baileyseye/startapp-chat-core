package startapp.com.core.views.users;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.validator.IntegerRangeValidator;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import com.vaadin.flow.router.RouteParameters;
import startapp.com.core.dto.UserDto;
import startapp.com.core.service.UserService;
import startapp.com.core.views.MainLayout;
import startapp.com.core.views.helper.NavigationHelper;

/**
 * Форма для создания и редактирования пользователя
 */
@PageTitle("Пользователь | Startapp Chat Core")
@Route(value = "users/new", layout = MainLayout.class)
@RouteAlias(value = "users/edit/{id}", layout = MainLayout.class)
public class UserFormView extends VerticalLayout implements BeforeEnterObserver {

    private final UserService userService;
    private final Binder<UserDto> binder = new Binder<>(UserDto.class);

    private TextField nameField;
    private IntegerField ageField;
    private Button saveButton;
    private Button cancelButton;
    private H2 formTitle;
    private Paragraph formSubtitle;

    private Long userId;
    private boolean isNewUser = true;

    public UserFormView(UserService userService) {
        this.userService = userService;

        setWidth("600px");
        setMaxWidth("100%");
        setSpacing(false);
        setPadding(false);

        initContent();
        initBinder();
    }

    private void initContent() {
        Div header = new Div();
        header.setWidthFull();
        header.getStyle().set("text-align", "center");
        header.getStyle().set("margin-bottom", "32px");

        H2 title = new H2();
        title.setId("form-title");
        formTitle = title;
        title.setText("Добавить пользователя");
        title.getStyle().set("margin", "0 0 8px 0");

        Paragraph subtitle = new Paragraph("Заполните информацию о новом пользователе");
        subtitle.setId("form-subtitle");
        formSubtitle = subtitle;
        subtitle.getStyle().set("color", "#6b7280");
        subtitle.getStyle().set("font-size", "0.875rem");
        subtitle.getStyle().set("margin", "0");

        header.add(title, subtitle);
        add(header);

        VerticalLayout form = new VerticalLayout();
        form.setWidthFull();
        form.setSpacing(true);
        form.setPadding(false);
        form.getStyle().set("max-width", "500px");
        form.getStyle().set("margin", "0 auto");

        nameField = new TextField("Имя *");
        nameField.setPlaceholder("Введите имя пользователя");
        nameField.setRequired(true);
        nameField.setHelperText("Обязательное поле");
        nameField.setWidthFull();
        form.add(nameField);

        ageField = new IntegerField("Возраст *");
        ageField.setPlaceholder("Введите возраст");
        ageField.setMin(1);
        ageField.setMax(150);
        ageField.setRequired(true);
        ageField.setHelperText("От 1 до 150 лет");
        ageField.setWidthFull();
        form.add(ageField);

        add(form);

        HorizontalLayout buttons = new HorizontalLayout();
        buttons.setWidthFull();
        buttons.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        buttons.setSpacing(true);
        buttons.getStyle().set("margin-top", "32px");

        cancelButton = new Button("Отмена");
        cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        cancelButton.setIcon(new Icon(VaadinIcon.CLOSE));
        cancelButton.addClickListener(e -> NavigationHelper.navigateBack());

        saveButton = new Button("Сохранить");
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveButton.setIcon(new Icon(VaadinIcon.CHECK));
        saveButton.addClickListener(e -> handleSave());

        buttons.add(cancelButton, saveButton);
        add(buttons);
    }

    private void initBinder() {
        binder.forField(nameField)
            .asRequired("Имя обязательно для заполнения")
            .bind(UserDto::getName, UserDto::setName);

        binder.forField(ageField)
            .asRequired("Возраст обязательно для заполнения")
            .withValidator(new IntegerRangeValidator("Возраст должен быть от 1 до 150", 1, 150))
            .bind(UserDto::getAge, UserDto::setAge);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        RouteParameters params = event.getRouteParameters();

        if (params != null && params.get("id").isPresent()) {
            try {
                userId = Long.parseLong(params.get("id").get());
                isNewUser = false;
                loadUser(userId);

                if (formTitle != null) {
                    formTitle.setText("Редактировать пользователя");
                }
                if (formSubtitle != null) {
                    formSubtitle.setText("Измените информацию о пользователе");
                }

                saveButton.setText("Сохранить изменения");
            } catch (NumberFormatException e) {
                Notification.show("Некорректный ID пользователя", 3000, Notification.Position.MIDDLE);
                NavigationHelper.navigateBack();
            }
        } else {
            isNewUser = true;
            userId = null;
            clearForm();
            if (formTitle != null) {
                formTitle.setText("Добавить пользователя");
            }
            if (formSubtitle != null) {
                formSubtitle.setText("Заполните информацию о новом пользователе");
            }
            saveButton.setText("Сохранить");
        }
    }

    private void loadUser(Long id) {
        try {
            UserDto user = userService.getUserById(id);
            binder.readBean(user);
        } catch (Exception e) {
            Notification.show("Пользователь не найден", 3000, Notification.Position.MIDDLE);
            NavigationHelper.navigateBack();
        }
    }

    private void clearForm() {
        UserDto emptyUser = new UserDto();
        binder.readBean(emptyUser);
    }

    private void handleSave() {
        if (!binder.isValid()) {
            Notification.show("Пожалуйста, исправьте ошибки в форме", 3000, Notification.Position.MIDDLE);
            return;
        }

        try {
            UserDto userDto = binder.getBean();

            if (isNewUser) {
                userDto.setId(null);
                userService.createUser(userDto);
                Notification.show("Пользователь успешно создан! ✓", 3000, Notification.Position.MIDDLE);
                NavigationHelper.navigateTo(UsersListView.class);
            } else {
                userDto.setId(userId);
                userService.updateUser(userId, userDto);
                Notification.show("Пользователь успешно обновлен! ✓", 3000, Notification.Position.MIDDLE);
                NavigationHelper.navigateTo(UsersListView.class);
            }
        } catch (Exception e) {
            Notification.show("Ошибка при сохранении: " + e.getMessage(), 5000, Notification.Position.MIDDLE);
        }
    }
}
