package startapp.com.core.views.auth;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import jakarta.servlet.http.HttpSession;
import startapp.com.core.dto.UserDto;
import startapp.com.core.service.AuthService;

@Route("login")
public class LoginView extends VerticalLayout {

    private final AuthService authService;
    private final HttpSession session;

    public LoginView(AuthService authService, HttpSession session) {
        this.authService = authService;
        this.session = session;
        setSizeFull();
        setJustifyContentMode(JustifyContentMode.CENTER);
        setAlignItems(Alignment.CENTER);
        getStyle().set("background", "linear-gradient(135deg, #667eea 0%, #764ba2 100%)");

        createForm();
    }

    private void createForm() {
        VerticalLayout formContainer = new VerticalLayout();
        formContainer.setWidth("350px");
        formContainer.setSpacing(true);
        formContainer.setPadding(true);
        formContainer.getStyle()
            .set("background", "#fff")
            .set("border-radius", "8px")
            .set("box-shadow", "0 4px 20px rgba(0,0,0,0.2)")
            .set("padding", "32px");

        var title = new com.vaadin.flow.component.html.H2("Вход в чат");
        title.getStyle().set("text-align", "center").set("margin-bottom", "24px");
        formContainer.add(title);

        TextField usernameField = new TextField("Имя пользователя");
        usernameField.setPlaceholder("Введите имя пользователя");
        usernameField.setWidthFull();

        PasswordField passwordField = new PasswordField("Пароль");
        passwordField.setPlaceholder("Введите пароль");
        passwordField.setWidthFull();

        formContainer.add(usernameField, passwordField);

        Button loginButton = new Button("Войти");
        loginButton.setWidthFull();
        loginButton.getStyle().set("background", "#5682ab").set("color", "#fff").set("border", "none");
        loginButton.addClickListener(e -> {
            String username = usernameField.getValue().trim();
            String password = passwordField.getValue();
            if (username.isEmpty() || password.isEmpty()) {
                Notification.show("Заполните все поля", 3000, Notification.Position.TOP_CENTER);
                return;
            }
            try {
                UserDto user = authService.login(username, password);
                session.setAttribute("userId", user.getId());
                session.setAttribute("username", user.getName());
                getUI().ifPresent(ui -> ui.navigate("chat"));
            } catch (Exception ex) {
                Notification.show("Неверное имя пользователя или пароль", 3000, Notification.Position.TOP_CENTER);
            }
        });

        // Обработка нажатия Enter в полях ввода
        usernameField.addKeyPressListener(Key.ENTER, e -> loginButton.click());
        passwordField.addKeyPressListener(Key.ENTER, e -> loginButton.click());

        Button registerButton = new Button("Зарегистрироваться");
        registerButton.setWidthFull();
        registerButton.getStyle().set("background", "#f0f0f0").set("color", "#333").set("border", "none");
        registerButton.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate("register")));

        formContainer.add(loginButton, registerButton);
        add(formContainer);
    }
}
