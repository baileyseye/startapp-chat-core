package startapp.com.core.views;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasElement;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.RouterLayout;
import jakarta.servlet.http.HttpSession;

/**
 * Основной макет приложения с боковой навигацией
 */
public class MainLayout extends HorizontalLayout implements RouterLayout, AfterNavigationObserver {

    private final Div contentContainer = new Div();
    private Tabs navigationTabs;
    private Tab selectedTab;
    private Tab dashboardTab;
    private Tab chatTab;
    private Tab usersTab;
    private final HttpSession session;
    private Button themeToggleBtn;

    public MainLayout(HttpSession session) {
        this.session = session;
        setWidthFull();
        setHeightFull();
        setPadding(false);
        setSpacing(false);
        getElement().getStyle().set("background", "var(--bg-color)");

        VerticalLayout sidebar = createSidebar();

        contentContainer.addClassName("main-content");
        contentContainer.setWidthFull();
        contentContainer.setHeightFull();
        contentContainer.getStyle().set("overflow-y", "auto");
        contentContainer.getStyle().set("padding", "32px");
        contentContainer.getStyle().set("background", "var(--bg-color)");

        createThemeToggle();
        add(sidebar, contentContainer);

        // Применяем сохранённую тему при загрузке
        applySavedTheme();
    }

    private VerticalLayout createSidebar() {
        VerticalLayout sidebar = new VerticalLayout();
        sidebar.addClassName("nav-container");
        sidebar.setWidth("280px");
        sidebar.setHeightFull();
        sidebar.setPadding(true);
        sidebar.setSpacing(false);
        sidebar.getStyle().set("background", "var(--card-bg)");
        sidebar.getStyle().set("border-right", "1px solid var(--border-color)");

        Div logo = new Div();
        logo.addClassName("logo-container");
        logo.setWidthFull();
        logo.getStyle().set("padding", "16px");
        logo.getStyle().set("margin-bottom", "24px");
        logo.getStyle().set("border-bottom", "1px solid var(--nav-border)");

        HorizontalLayout logoLayout = new HorizontalLayout();
        logoLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        logoLayout.setSpacing(true);

        Icon logoIcon = new Icon(VaadinIcon.CHAT);
        logoIcon.getStyle().set("color", "#6366f1");
        logoIcon.getStyle().set("font-size", "1.5rem");

        Span logoText = new Span("Startapp Chat Core");
        logoText.addClassName("logo-text");
        logoText.getStyle().set("font-size", "1.25rem");
        logoText.getStyle().set("font-weight", "700");

        logoLayout.add(logoIcon, logoText);
        logo.add(logoLayout);

        navigationTabs = new Tabs();
        navigationTabs.setOrientation(com.vaadin.flow.component.tabs.Tabs.Orientation.VERTICAL);
        navigationTabs.setWidthFull();

        dashboardTab = new Tab(new Icon(VaadinIcon.HOME));
        chatTab = new Tab(new Icon(VaadinIcon.CHAT));
        usersTab = new Tab(new Icon(VaadinIcon.USERS));

        dashboardTab.getElement().setAttribute("aria-label", "Dashboard");
        chatTab.getElement().setAttribute("aria-label", "Чаты");
        usersTab.getElement().setAttribute("aria-label", "Пользователи");

        navigationTabs.add(dashboardTab, chatTab, usersTab);
        navigationTabs.setSelectedTab(dashboardTab);
        selectedTab = dashboardTab;

        navigationTabs.addSelectedChangeListener(e -> {
            Tab tab = navigationTabs.getSelectedTab();
            if (selectedTab != tab) {
                if (tab.equals(dashboardTab)) {
                    getUI().ifPresent(ui -> ui.navigate("dashboard"));
                } else if (tab.equals(chatTab)) {
                    getUI().ifPresent(ui -> ui.navigate("chat"));
                } else {
                    getUI().ifPresent(ui -> ui.navigate("users"));
                }
                selectedTab = tab;
            }
        });

        // Информация о пользователе и кнопка выхода
        Div userSection = new Div();
        userSection.addClassName("user-section");
        userSection.setWidthFull();
        userSection.getStyle().set("margin-top", "auto");
        userSection.getStyle().set("padding-top", "16px");
        userSection.getStyle().set("border-top", "1px solid var(--nav-border)");

        String username = (String) session.getAttribute("username");
        if (username != null) {
            Span userLabel = new Span("👤 " + username);
            userLabel.addClassName("user-label");
            userLabel.getStyle().set("font-size", "0.95rem");
            userLabel.getStyle().set("margin-bottom", "8px");
            userSection.add(userLabel);
        }

        Button logoutButton = new Button("Выйти", new Icon(VaadinIcon.SIGN_OUT));
        logoutButton.addClassName("logout-button");
        logoutButton.setWidthFull();
        logoutButton.getStyle().set("background", "#fee2e2");
        logoutButton.getStyle().set("color", "#dc2626");
        logoutButton.getStyle().set("border", "none");
        logoutButton.addClickListener(e -> {
            session.invalidate();
            getUI().ifPresent(ui -> ui.navigate("login"));
        });

        userSection.add(logoutButton);
        sidebar.add(logo, navigationTabs, userSection);
        return sidebar;
    }

    private void createThemeToggle() {
        themeToggleBtn = new Button();
        themeToggleBtn.setIcon(new Icon(VaadinIcon.MOON));
        themeToggleBtn.getStyle()
            .set("position", "fixed")
            .set("top", "20px")
            .set("right", "300px")
            .set("width", "48px")
            .set("height", "48px")
            .set("border-radius", "50%")
            .set("background", "linear-gradient(135deg, #667eea, #764ba2)")
            .set("border", "none")
            .set("box-shadow", "0 4px 20px rgba(102, 126, 234, 0.4)")
            .set("cursor", "pointer")
            .set("z-index", "1000")
            .set("color", "#fff")
            .set("font-size", "1.2rem")
            .set("display", "flex")
            .set("align-items", "center")
            .set("justify-content", "center");
        themeToggleBtn.addClickListener(e -> toggleTheme());
        getElement().appendChild(themeToggleBtn.getElement());
    }

    private void applySavedTheme() {
        com.vaadin.flow.component.UI.getCurrent().getPage().executeJs(
            "const savedTheme = localStorage.getItem('theme');" +
            "if (savedTheme === 'dark') {" +
            "  document.body.classList.add('theme-dark');" +
            "  return true;" +
            "} else if (savedTheme === 'light') {" +
            "  document.body.classList.remove('theme-dark');" +
            "  return false;" +
            "} else {" +
            "  const prefersDark = window.matchMedia('(prefers-color-scheme: dark)').matches;" +
            "  if (prefersDark) {" +
            "    document.body.classList.add('theme-dark');" +
            "    return true;" +
            "  }" +
            "  return false;" +
            "}"
        ).then(Boolean.class, isDark -> {
            if (Boolean.TRUE.equals(isDark)) {
                themeToggleBtn.setIcon(new Icon(VaadinIcon.LIGHTBULB));
            } else {
                themeToggleBtn.setIcon(new Icon(VaadinIcon.MOON));
            }
        });
    }

    private void toggleTheme() {
        com.vaadin.flow.component.UI.getCurrent().getPage().executeJs(
            "const body = document.body;" +
            "const isDark = body.classList.contains('theme-dark');" +
            "if (isDark) {" +
            "  body.classList.remove('theme-dark');" +
            "  localStorage.setItem('theme', 'light');" +
            "  return 'moon';" +
            "} else {" +
            "  body.classList.add('theme-dark');" +
            "  localStorage.setItem('theme', 'dark');" +
            "  return 'lightbulb';" +
            "}"
        ).then(String.class, icon -> {
            if ("lightbulb".equals(icon)) {
                themeToggleBtn.setIcon(new Icon(VaadinIcon.LIGHTBULB));
            } else {
                themeToggleBtn.setIcon(new Icon(VaadinIcon.MOON));
            }
        });
    }

    @Override
    public void showRouterLayoutContent(HasElement content) {
        contentContainer.removeAll();
        if (content instanceof Component component) {
            component.getElement().getStyle().set("animation", "fadeIn 0.5s ease-out");

            // Убираем отступы для страницы чата
            String className = component.getElement().getClass().getName();
            if (className.contains("ChatView")) {
                component.getElement().getStyle().set("margin", "0");
                component.getElement().getStyle().set("padding", "0");
                contentContainer.getStyle().set("padding", "0");
                contentContainer.getStyle().set("background", "transparent");
            } else {
                contentContainer.getStyle().set("padding", "32px");
                contentContainer.getStyle().set("background", "var(--bg-color)");
            }
            
            contentContainer.add(component);
        }
    }

    @Override
    public void afterNavigation(AfterNavigationEvent event) {
        String path = event.getLocation().getPath();

        if (path.isEmpty() || path.equals("dashboard")) {
            navigationTabs.setSelectedTab(dashboardTab);
        } else if (path.equals("chat") || path.startsWith("chat/")) {
            navigationTabs.setSelectedTab(chatTab);
        } else if (path.startsWith("users")) {
            navigationTabs.setSelectedTab(usersTab);
        }

        String title = getTitleForPath(path);
        com.vaadin.flow.component.UI.getCurrent().getPage().setTitle(title);
    }

    private String getTitleForPath(String path) {
        if (path.isEmpty() || path.equals("dashboard")) {
            return "Dashboard | Startapp Chat Core";
        } else if (path.equals("chat") || path.startsWith("chat/")) {
            return "Чаты | Startapp Chat Core";
        } else if (path.equals("users")) {
            return "Пользователи | Startapp Chat Core";
        } else if (path.startsWith("users/new")) {
            return "Добавить пользователя | Startapp Chat Core";
        } else if (path.startsWith("users/edit")) {
            return "Редактировать пользователя | Startapp Chat Core";
        }
        return "Startapp Chat Core";
    }
}